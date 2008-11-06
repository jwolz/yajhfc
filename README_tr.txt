YajHFC 0.3.9 
=======================

HAKKINDA
-----

YajHFC (Yet Another Java HylaFAX Client), HylaFAX faks sunucusu
(http://www.hylafax.org/) için Java'da yazılmış istemcidir.

Özellikleri:
* PostScript, PDF ve çeşitli diğer biçimlerde belgeleri faxlama
* Faksları sorgulama
* Şablonlardan kapak sayfası oluşturmayı destekler
* Alınan ve gönderilen faksları görüntüleme
* Telefon Defteri
* Bir iletişimde görünür tablo sütunlarını seçebilme
* 6 Dil desteği: İngilizce, Fransızca, Almanca, Rusça, İspanyolca ve Türkçe

Web sitesi: http://yajhfc.berlios.de/
email:    Jonas Wolz <jwolz@freenet.de>

KURULUM
------------

Gereksinimler:
* JRE 5.0 yada üstü
* Yerel ağınızda çalışır durumda bir HylaFAX sunucusu (elbette ;-) )
* TIFF dosyalarını görebilmek için yazılım ( SSS'ye bakaniz )
* İsteğe bağlı: PostScript görüntüleyici (örn. Ghostview)

Kurulum için sadece YajHFC'yi dosya sisteminizdeki herhangi bir dizine
indirin.
Çalıştırmak için "java -jar yajhfc-0_3_9.jar" kullanın.
(Windows'da genellikle jar dosyasına iki kez tıklayabilirsiniz)

LİSANS
-------

YajHFC GPL altında özgür yazılım lisanslıdır.
ayrıntılar için KOPYALAMA dosyasına bakınız.

KAYNAK KOD
-----------

YajHFC'nin kaynak koddan nasıl yapılandırılğını açıklayan bilgiler için
lütfen bu dizindeki BUILDING.txt dosyasına bakın.

YajHFC aşağıdaki paketleri kullanır:
(1) Gnu.hylafax kütüphaneleri (çekirdek ve inet-ftp paketi)
    http://gnu-hylafax.sourceforge.net/
(2) TableLayout.jar
    https://tablelayout.dev.java.net/
(3) "Java look and feel Graphics Repository" (jlfgr-1_0.jar)
    http://java.sun.com/developer/techDocs/hi/repository/
(4) (1). bölümdekinin gerektirdiği Apache Common Logging
    http://commons.apache.org/logging/

(2), (3) ve (4). bölümdeki gerekli dosyaların kopyaları aynı zamanda 
jar dosyasının altdizinlerindeki kaynak arşivinde de bulunur.

YajHFC bir ilkten daha karmaşık ("hello world" programlarından daha gelişmiş) 
Java projesi olarak Eclipse IDE kullanılarak özgün biçimde yazılmıştır.

DEĞİŞİKLİKLER
-------

0.3.9:
- Bir kaç hata düzeltildi
- İtelyanca çeviri eklendi 

0.3.8a:
- XML telefon defteri hatası giderildi.
- Kullanıcı Arayüzünde ufak iyileştirilmeler yapıldı

0.3.8:
- Çoklu telefon defteri desteği için gelişmiş telefon defteri penceresi 
- Faks okundu/okunmadı durumunu merkezi veritabanı tablosuna kaydetme desteği
- JDBC sürücüleri ve eklentiler eklemek için görsel panel
- YajHFC Rusca çevirisi eklendi.

0.3.7:
- Yeni sadeleştirilmiş gönderi iletişimi
- HTML biçiminde kapak sayfası desteği
- Eklenti kullanarak XSL:FO ve ODT biçimlerinde kapak sayfası desteği
- Daha iyi eklenti desteği
- Günlük tutma, Java logging APIleri kullanacak şekilde değiştirildi
- Şu an gnu.hylafax 1.0.1 kullanılıyor
- Bazı hatalar düzeltildi

0.3.6:
- Bazı hatalar giredildi
- Gönder iletişimi için çoklu telefon defteri seçme desteği
- Hata giderme modunda günlük dosyası oluşturmak için yeni komut satırı değişkeni ("--logfile") 
- Bazı HylaFAX hatalarını çözeceği olaşılığı ile "bağlantı kesik yönetemi" desteği
- Kullanılacak modemi seçme desteği

0.3.5:
- Kullanıcı arayüzü iyileştirmesi/"cilası" (örn.: faks listesi için ilerleme çubuğu ve daha fazla simge)
- Görünür sütunlardaki bazı kısıtlamalar kaldırıldı
  (örn.: İşlem ID (kimlik no) görünür olmaz zorunda değil)
- Yeni faks işlemleri şimdi yanlızca fasklar "işlemde değil"den *sonra*
  çalıştırılıyor

0.3.4a:
- Hata düzeltme sürümü:
  Standart girdi tarafından çalışan bir kopyaya bir faks gönderildiğinde 
  0.3.4'de fazladan bir karakter eklendi

0.3.4:
- Komut satırından alıcıları tanımlama desteği
- Faks işlemleri için "askıya alma/devam etme"
- Fransızca çeviri eklendi
- İlk gösterilen sekmeyi bir komut satırı değişkeni tarafından ayarlama
- (bazı hataları gideren) "Ana" gnu.hylafax kütüphanesi şimdi öntanımlı olarak kullanılıyor

0.3.3:
- Tekrar faks gönderme desteği
- Gelen/giden faks tablolarını yazdırma desteği
- Başarısız faks işlemleri için renkli artalan
- Salt okunur LDAP telefon desteği eklendi
- Birden fazla açılabilen telefon defteri desteği
- Kendiliğinden "faks yazıcısı" kurulumu ile Windows kurulum programı

0.3.2:
- Faks gönderirken ara sıra görülen hatalar giderildi
- Artalanda yeni bir kopya çalıştırma desteği (faks yazıcıları için kullanışlı)
- Yeni komut satırı değişkenleri
- Tazeleme düğmesi
- Kaynak dağıtıma karınca derleme dosyası eklendi
- Belgelendirme güncellendi

0.3.1:
- Görünüm değiştirebilme
- Yeni faksları kendiliğinden bir gösterici ile görüntüleme seçeneği
- HylaFAX sunucu sorgulama sıklığını değiştirme kullanıcı arayüzü seçeneği 
- Telefon defteri için sade bir arama iletişimi eklendi
- YajHFC şimdi gnu.haylafax kütüphanesi "ana" sürümü ile çalışıyor (iyice kontrol edilmedi!)

0.3.0:
- Bir faks işlemini teslim etmeden resimleri PostScript'e dönüştürme desteği eklendi
- Gönder iletişimine bir "önizleme" düğmesi eklendi
- SQL veritabanında (JDBC kullanarak) telefon defteri desteği eklendi

0.2.7a:
- "Sadece sahip olunan faksları"ı görüntülerken oluşan istisna durum düzeltildi

0.2.7:
- İspanyolca çeviri eklendi
- Dil seçebilme eklendi
- Tarih değerine saat dilimi ekleme desteği
- Gönderme esnasında rakamların yanlış gösterilmesine neden olan hata giderildi
- Çoklu faks

0.2.6:
- Windows 9x için küçük çözümler

0.2.5:
- Ufak hatalar giderildi

0.2.4:
- Filtreleme desteği
- Yönetici kipi desteği

0.2.3:
- Giden fakslar için çoklu telefon nuraması/dosya desteği
- Faks iletme/kaydetme 
- İç değişikler

0.2.2:
- "herzaman üstte"/yeni faks alındığında bip sesi desteği eklendi 

0.2.1:
- Gelen fakslar için okundu/okunmadı durum bilgisi eklendi
- Telefon defteri artık sıralı
- Bazı iç değişikler

0.2:
- Faks kapak sayfası desteği eklendi
- Sorgu desteği eklendi

YAPILACAKLAR LİSTESİ
----

Aşağıdaki özellikler gelecekte eklenebilir:

* Daha fazla dilde çeviri?

YajHFC, çeviri için GNU gettext kullanır yani programa bunları 
eklemek çok kolaydır.
Tercüman için (belgelendirmeyi çevirmeden) yeni çeviri oluşturmak için ön çalışmaya
bir kaç saat ve her yeni sürüme yaklaşık bir saat ayırmak gerekir.
Bunu yapmak için "İleri" seviye teknik bilgiye ihtiyacınız yok (sadece bir
metin düzenleyici ile nasıl çalışacağınızı bilmeniz yeterli,
ki özelleştirilmiş yazılım kurarak çok daha kolaylaşır).

Kısaca, eğer ana dilinize çevirilmiş bir YajHFC isterseniz, tercümanların daima başımın üstünde
yeri var. ;-)
