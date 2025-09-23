import requests
import concurrent.futures
import time

# Endpoint API Spring Boot yang sedang diuji dekonstruksinya
URL = "http://localhost:8181/api/v1/submissions"

# Payload tiruan yang mensimulasikan kode tugas mahasiswa
PAYLOAD = {"code": "public class Main { public static void main(String[] args) { System.out.println(\"Test\"); } }"}

# Simulasi beban ekstrem: 5.000 mahasiswa melakukan submisi dalam waktu bersamaan
TOTAL_REQUESTS = 5000  

# Batas concurency tingkat OS/Klien (200 pekerja simultan menembak server)
CONCURRENCY = 200      

def send_request():
    try:
        # Batasan timeout 5 detik untuk mendeteksi apakah server mengalami 'freeze' atau 'livelock'
        response = requests.post(URL, json=PAYLOAD, timeout=5)
        return response.status_code
    except Exception as e:
        # Menangkap kegagalan koneksi di tingkat jaringan (misal: Connection Refused akibat TCP backlog penuh)
        return "Error"

print(f"Menembakkan {TOTAL_REQUESTS} requests ke {URL}...")
start_time = time.time()

results = []
# Menggunakan ThreadPoolExecutor Python untuk mensimulasikan beban konkurensi tinggi dari sisi klien
with concurrent.futures.ThreadPoolExecutor(max_workers=CONCURRENCY) as executor:
    futures = [executor.submit(send_request) for _ in range(TOTAL_REQUESTS)]
    for future in concurrent.futures.as_completed(futures):
        results.append(future.result())

end_time = time.time()

# Menghitung metrik keberhasilan berdasarkan respons HTTP untuk analisis arsitektur
status_202 = results.count(202) # Indikator request berhasil masuk antrean memori internal
status_503 = results.count(503) # Indikator mekanisme penolakan beban (Backpressure) aktif bekerja
error_count = results.count("Error") # Indikator server mengalami crash total/drop di layer TCP
other_status = len(results) - (status_202 + status_503 + error_count) # Biasanya mendeteksi HTTP 500 jika exception belum dihandle

print(f"Selesai dalam {end_time - start_time:.2f} detik.")
print(f"Total 202 Accepted (Diterima): {status_202}")
print(f"Total 503 Service Unavailable (Ditolak/Backpressure): {status_503}")
print(f"Total Connection Error/Timeout: {error_count}")
print(f"Total Status Lainnya: {other_status}")