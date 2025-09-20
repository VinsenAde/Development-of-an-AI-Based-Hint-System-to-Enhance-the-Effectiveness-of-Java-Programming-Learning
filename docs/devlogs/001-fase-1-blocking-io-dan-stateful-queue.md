Devlog #001: Masalah "Pelayan Menunggu" & Kenapa Antrean Memori Lokal Itu Berbahaya
Tanggal: 03 September 2025 | Branch: experiment/java-executor-service | Status: Ditinggalkan (Scrapped)

The Trigger: Bom Waktu di Balik Kode Skripsi
Setelah sidang skripsi selesai, saya mengevaluasi ulang platform pembelajaran atau online judge ini. Untuk skala pengujian lab dengan 30 mahasiswa, sistem berjalan lancar. tapi, saya menyadari ada kelemahan struktural yang fatal: Blocking I/O.

saat siswa mengirim kode, server Tomcat menerima request tersebut, memanggil ProcessBuilder ke OS untuk kompilasi, dan menunggu sampai kompilasi selesai. Jika ada 20 mahasiswa mengirim kode bersamaan, tomcat akan kehabisan thread dan user ke-21 bakal mendapat error Gateway Timeout.

Pertanyaannya: Bagaimana caranya membebaskan thread utama HTTP, agar server bisa langsung merespons user, sementara kompilasi yang berat tetap berjalan di latar belakang?

The Naive Solution: Java ThreadPool
pendekatan pertama saya cukup naif: "Gunakan saja fitur bawaan Java". Saya memindahkan tugas eksekusi ke memori lokal menggunakan ThreadPoolExecutor dan LinkedBlockingQueue (batas 100 antrean).

private final ExecutorService executorService = new ThreadPoolExecutor(
2, 4, 60L, TimeUnit.SECONDS,
new LinkedBlockingQueue<>(100)
);

secara teori ini berhasil. Saat kode dikirim, server langsung membalas HTTP 202 Accepted secara instan, dan proses berat dialihkan ke background thread. Masalah selesai? sayangnya tidak.

The Reality Check: Metafora Restoran yang Kacau
saat saya melakukan stress-testing, saya sadar sistem ini justru menciptakan bencana yang lebih besar. kalau diibaratkan sebuah restoran, ini yang terjadi:

Dulu, Pelayan (Thread Tomcat) saya selalu menunggu Koki (CPU/Kompilator) memasak di dapur, mengabaikan pelanggan baru. Solusi ThreadPool di atas ibarat saya memberi Pelayan sebuah Tusukan Nota (Order Spindle) di meja kasir. pelayan tinggal menancapkan pesanan di sana dan kembali melayani tamu, sementara koki mengambilnya satu per satu.

terdengar efisien, sampai dua bencana ini terjadi:

Kehilangan Data (Stateful RAM): Tusukan nota itu terbuat dari kertas biasa (In-Memory Queue). kalau restoran tiba-tiba mati lampu, atau server saya harus di-restart untuk pembaruan, semua antrean kode siswa yang belum selesai dieksekusi hangus tak berbekas. sistem produksi butuh antrean yang tidak hilang walau server mati (Persistent).

Kelumpuhan Server (Resource Contention): Bagaimana jika ada siswa usil mengirim kode "while(true)"? koki (Thread Latar Belakang) akan terjebak selamanya memasak makanan yang tidak bisa matang. core CPU bakal tersedot 100%. karena dapur dan ruang makan berada di gedung yang sama (Satu Server/OS), asap dari dapur mengepul ke depan dan melumpuhkan seluruh restoran (Server Freeze).

The Takeaway
mencoba mengatur multi-threading secara internal untuk tugas berat (seperti kompilasi kode OS) sangat berbahaya.

Insight terbesarnya: dapur (komputasi eksekusi) harus dipisah secara fisik dari ruang makan (API Server). saya butuh dapur terpisah di gedung lain (Worker Node/Decoupling), dan sistem kurir pesanan yang kebal api (External Message Broker seperti AWS SQS).

eksperimen berikutnya, saya akan membuang antrean memori lokal ini dan mencoba arsitektur Message Broker.

Melalui penulisan kode ini, saya memahami parameter penting dalam manajemen thread Java:

corePoolSize (2): Jumlah thread minimal yang selalu siaga.

maximumPoolSize (4): Batas thread maksimal yang bisa dibuka jika antrean penuh.

keepAliveTime: Waktu hidup thread tambahan sebelum dihancurkan jika menganggur.

Bounded Queue (100): Membatasi antrean agar RAM tidak meledak terkena serangan Out-Of-Memory.