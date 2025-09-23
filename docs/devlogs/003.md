DevLog 002: Eksperimen ThreadPoolTaskExecutor dan Batasan Memori Antrean (Unbounded vs Bounded Queue)
Konteks Eksperimen
Melanjutkan evaluasi dari arsitektur awal aplikasi yang bersifat sinkron dan memicu pemblokiran thread HTTP Tomcat akibat eksekusi langsung perintah ProcessBuilder, saya mencoba memindahkan beban kompilasi tersebut ke latar belakang menggunakan ThreadPoolTaskExecutor bawaan Spring Boot. Tujuan awal adalah menerapkan prinsip Non-blocking I/O agar server API utama dapat segera memberikan respons HTTP 202 Accepted tanpa harus menunggu proses kompilasi kode yang memakan waktu lama selesai dijalankan oleh sistem operasi.

Hipotesis Awal (Perspektif Sebelum Pengujian)
Daripada membiarkan thread HTTP Tomcat membeku menunggu ProcessBuilder selesai mengompilasi Java/Python, saya akan melemparkan tugas eksekusi tersebut ke dalam antrean latar belakang (ThreadPoolExecutor). Dengan begitu, Tomcat bisa langsung menjawab HTTP 202 Accepted, siswa tidak perlu menunggu loading lama di browser, dan server tetap dingin.

Implementasi Kode Tahap 1: Unbounded Queue (Kapasitas Tanpa Batas)
Pada implementasi pertama, saya mendaftarkan konfigurasi executor di mana kapasitas antrean memori dibiarkan menggunakan nilai default berupa Integer.MAX_VALUE.

Java
@Configuration
@EnableAsync
public class ExecutorConfig {

    @Bean(name = "submissionExecutor")
    public Executor submissionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(Integer.MAX_VALUE); // Unbounded Queue
        executor.setThreadNamePrefix("SubExecutor-");
        executor.initialize();
        return executor;
    }
}
Di tingkat REST Controller, tugas pemrosesan dilempar secara asinkron melalui service:

Java
@PostMapping("/submissions")
public ResponseEntity<Map<String, String>> submitCode(@RequestBody Map<String, String> payload) {
    submissionService.processSubmissionAsync(payload.getOrDefault("code", "print('hello')"));
    return ResponseEntity.accepted().body(Map.of(
            "status", "PENDING",
            "message", "Kode diterima dan masuk antrean eksekusi."
    ));
}
Skenario Pengujian Beban
Untuk mensimulasikan situasi nyata saat ratusan mahasiswa mengumpulkan tugas secara bersamaan, saya membuat sebuah skrip pengujian beban menggunakan Python yang menembakkan total 5.000 request dengan konkurensi sebanyak 200 virtual users secara serentak. Di dalam service, setiap proses sengaja ditahan selama 3 detik menggunakan Thread.sleep untuk meniru durasi kompilasi yang sesungguhnya.

Hasil Pengujian 1: Ilusi Kecepatan pada Unbounded Queue
Skrip pengujian Python memberikan metrik performa sebagai berikut:

adewi@DaVincent MINGW64 /d/NetBeansProjects/selesai/java-ai (14)/java-ai (7)/java-ai/java-ai/test (experiment/java-executor-service)
$ python stress_test.py
Menembakkan 5000 requests ke http://localhost:8181/api/v1/submissions...
Selesai dalam 7.81 detik.
Total 202 Accepted (Diterima): 5000
Total 503 Service Unavailable (Ditolak/Backpressure): 0
Total Connection Error/Timeout: 0
Total Status Lainnya: 0


Analisis Masalah Tersembunyi:
Metrik di atas memunculkan ilusi keberhasilan karena dari sisi klien semua request selesai dalam waktu kurang dari 8 detik dengan status sukses 202 Accepted. Namun, analisis internal pada JVM Spring Boot menunjukkan kondisi kritis yang membahayakan sistem:

Resource Exhaustion & Livelock: Sebanyak 5.000 objek request baru saja dipaksa masuk ke dalam memori RAM (LinkedBlockingQueue).

Keterbengkalan Eksekusi (Backlog): Dengan konfigurasi maksimal 4 thread eksekutor dan durasi 3 detik per tugas, server membutuhkan waktu (5000 / 4) * 3 = 3.750 detik atau sekitar 62,5 menit untuk menyelesaikan antrean tersebut setelah skrip pengujian ditutup.

Risiko Kehilangan Data (Data Loss): Karena antrean ini hidup di dalam lifecycle RAM JVM, apabila server mengalami crash akibat kehabisan memori (OutOfMemoryError) atau dilakukan restart secara mendadak di tengah jalan, seluruh sisa antrean tugas mahasiswa akan hilang total tanpa jejak.

Implementasi Kode Tahap 2: Menerapkan Pola Backpressure (Bounded Queue)
Untuk mengatasi masalah penumpukan muatan di dalam memori, saya mengubah pendekatan dengan menerapkan pembatasan antrean yang ketat (Bounded Queue) dan menambahkan kebijakan penolakan tugas (AbortPolicy).

Pada ExecutorConfig.java, kapasitas antrean diperkecil menjadi 10 dan handler penolakan ditambahkan secara eksplisit:

Java
executor.setQueueCapacity(10); // Batasan ketat di RAM
executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
Pada SubmissionController.java, saya menangkap pengecualian spesifik RejectedExecutionException agar aplikasi mengembalikan status HTTP 503 Service Unavailable secara instan ketika antrean penuh:

Java
@PostMapping("/submissions")
public ResponseEntity<Map<String, String>> submitCode(@RequestBody Map<String, String> payload) {
    try {
        submissionService.processSubmissionAsync(payload.getOrDefault("code", "print('hello')"));
        return ResponseEntity.accepted().body(Map.of("status", "PENDING", "message", "Masuk antrean."));
    } catch (RejectedExecutionException e) {
        return ResponseEntity.status(503).body(Map.of(
            "status", "REJECTED",
            "message", "Server sibuk, antrean penuh. Coba lagi nanti."
        ));
    }
}
Hasil Pengujian 2: Mekanisme Fail-Fast Berhasil
Ketika skrip pengujian beban dijalankan kembali terhadap konfigurasi baru ini, hasil metrik berubah menjadi:

Plaintext
Menembakkan 5000 requests ke http://localhost:8181/api/v1/submissions...
Selesai dalam 8.99 detik.
Total 202 Accepted (Diterima): 22
Total 503 Service Unavailable (Ditolak/Backpressure): 4978
Total Connection Error/Timeout: 0
Total Status Lainnya: 0
Analisis Keberhasilan:
Sistem kini bertindak secara protektif dan "fail-fast". Server hanya menerima 22 request awal yang masuk ke dalam alokasi thread pool dan kapasitas antrean aman yang tersedia di RAM. Sisa 4.978 request lainnya langsung ditolak seketika menggunakan status 503 dalam waktu 8,99 detik. Melalui pendekatan ini, terminal Spring Boot tetap stabil, responsif, dan terhindar dari java.lang.OutOfMemoryError karena server berhasil mempertahankan batas kemampuan dirinya sendiri dari lonjakan beban eksternal.

Pelajaran Rekayasa Arsitektur yang Dipetik
Eksperimen dari kedua branch mati (graveyard branches) ini membuka sebuah kesadaran fundamental dalam perancangan platform engineering:

Menyelamatkan thread HTTP Tomcat dengan memindahkan tugas berat ke dalam antrean memori internal aplikasi tanpa batas (Unbounded Queue) adalah sebuah bom waktu yang menciptakan ilusi responsivitas semu.

Menerapkan pola Backpressure lokal menggunakan Bounded Queue berhasil mengamankan stabilitas server agar tidak crash, namun mengorbankan pengalaman pengguna karena sebagian besar request mahasiswa ditolak mentah-mentah dengan error 503.

Kesimpulan Akhir: Aplikasi monolitik ini harus didekonstruksi lebih jauh. Penampungan antrean tugas tidak boleh dibebankan pada memori internal JVM Spring Boot ataupun memotong request HTTP pengguna secara paksa. Desain arsitektur berikutnya harus mengeksternalisasi antrean ini ke sebuah Message Broker terpisah yang persisten agar dapat menampung ribuan request secara aman tanpa mengorbankan ketersediaan server API utama.

stress_test.py
1. Tujuan Validasi Skrip
Skrip ini ditulis bukan sekadar untuk melakukan pengujian beban, melainkan sebagai alat uji validasi arsitektur untuk membandingkan dua pendekatan manajemen antrean internal pada Spring Boot, yaitu Unbounded Queue melawan Bounded Queue (Backpressure).

2. Cara Membaca Kasus Kegagalan (Unbounded Queue: Integer.MAX_VALUE)
Jika konfigurasi server menggunakan kapasitas antrean tanpa batas, skrip ini akan selesai dengan sangat cepat (di bawah 8 detik) dan melaporkan Total 202 Accepted: 5000.

Penjelasan Teknis: Ini adalah kondisi Illusion of Speed. Sisi klien melihat seolah-olah sistem sangat cepat, namun di balik layar, memori heap JVM server dipaksa menampung 5.000 objek antrean. Dengan kapasitas proses server yang terbatas, server akan terjebak dalam siklus Garbage Collection thrashing selama berjam-jam, mengalami livelock, dan rentan kehilangan data total jika server mati mendadak.

3. Cara Membaca Kasus Keberhasilan (Bounded Queue: Kapasitas Ketat)
Jika konfigurasi server menggunakan batasan ketat (misal: kapasitas antrean 10) ditambah AbortPolicy, skrip ini akan menampilkan angka Total 202 Accepted yang sangat rendah (sekitar 20-an) dan sisanya berpindah ke Total 503 Service Unavailable: 4970+.

Penjelasan Teknis: Ini membuktikan bahwa pola Backpressure di tingkat aplikasi berjalan sukses. Karakteristik sistem berubah menjadi fail-fast. Server secara sadar menolak beban berlebih yang dapat merusak dirinya sendiri demi mempertahankan stabilitas dan ketersediaan sistem inti agar tidak mengalami crash akibat OutOfMemoryError.

