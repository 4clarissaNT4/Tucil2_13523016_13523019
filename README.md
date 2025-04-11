# Tugas Kecil 2 IF2211 Strategi Algoritma
### Repositori ini berisi program kompresi gambar dengan struktur data QuadTree yang mengimplementasikan algoritma Divide and Conquer.
![creators](https://github.com/4clarissaNT4/Tucil2_13523016_13523019/blob/main/doc/image_tucil2.jpg)
<div id="contributor">
  <strong>
    <h3>Kontributor</h3>
    <table align="center">
      <tr>
        <td>NIM</td>
        <td>Nama</td>
      </tr>
      <tr>
        <td>13523016</td>
        <td>Clarissa Nethania Tambunan</td>
      </tr>
      <tr>
        <td>13523019</td>
        <td>Shannon Aurellius Anastasya Lie</td>
    </table>
  </strong>
</div>

## Deskripsi Singkat
Algoritma Divide and Conquer adalah pendekatan pemecahan masalah dengan membagi persoalan utama menjadi beberapa upa-persoalan yang serupa namun berukuran lebih kecil (divide), menyelesaikan tiap upa-persoalan tersebut secara langsung atau rekursif (conquer), lalu menggabungkan solusi-solusinya untuk membentuk solusi akhir dari persoalan semula (combine). Objek yang dibagi bisa berupa larik, matriks, eksponen, atau bentuk input lainnya, tergantung konteks masalahnya. Karena setiap upa-persoalan memiliki karakteristik yang sama dengan persoalan utama, metode ini secara alami cocok diimplementasikan secara rekursif.

## Implementasi Algoritma Divide and Conquer
<b>1. Divide (Pembagian Blok) </b><br/>
Gambar yang diterima melalui input dibagi menjadi blok persegi. Jika blok tersebut tidak seragam (misalnya, nilai error-nya melebihi threshold), maka blok tersebut dibagi lagi menjadi empat bagian (kuadran): kiri-atas, kanan-atas, kiri-bawah, dan kanan-bawah. Hal ini dilakukan melalui fungsi kompresi yang memanggil dirinya sendiri secara rekursif. <br /><br />
<b>2. Conquer (Kompresi Sub-blok) </b><br />
Masing-masing dari keempat sub-blok tersebut kemudian diperiksa kembali apakah mereka masih perlu dibagi. Jika sudah cukup seragam (berdasarkan metode error dan threshold yang dipilih), maka blok tersebut tidak dibagi lagi (berhenti melakukan pembagian), dan diwakili oleh satu warna rata-rata. <br /><br />
<b>3. Combine (Penggabungan Hasil) </b><br />
Setelah semua blok telah diproses, gambar hasil kompresi dibentuk dari representasi tiap simpul daun pada pohon Quadtree. Proses ini juga menghasilkan gambar kompresi akhir dan animasi GIF yang menunjukkan transisi kompresi dari akar hingga kedalaman terakhir. <br /><br />

## Struktur Program
```
Tucil2_13523016_13523019/
├── bin/
├── doc/
├── src/
│   ├── GIFGenerator.java
│   ├── ImageCompressor.java
│   ├── Input.java
│   ├── Main.java
│   ├── Output.java
├── test/
├── README.md
```

## Tech Stack
- Java

## Cara Menjalankan Program
- Clone repositori ini dengan cara mengetik ini di terminal:
```shell
git clone https://github.com/4clarissaNT4/Tucil2_13523016_13523019.git
```

- Navigasi ke repositori yang sudah di-clone, lalu arahkan ke folder source codenya:
```shell
cd src
```

- Lakukan proses kompilasi dengan mengetik kode berikut pada terminal:
```shell
javac -d ../bin Main.java
java -cp ../bin Main
```

- Setelah itu cara menjalankan program untuk siap menerima input yaitu:
```shell
./main
```
