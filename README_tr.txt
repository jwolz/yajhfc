YajHFC 0.5.1 için BENİOKU
=======================

HAKKINDA
-----

YajHFC (Yet Another Java HylaFAX Client), HylaFAX faks sunucusu
(http://www.hylafax.org/) için Java'da yazılmış istemcidir.

Özellikleri:
* PostScript, PDF ve çeşitli diğer biçimlerde belgeleri fakslama
* Yoklama çağrısı
* Şablonlardan kapak sayfası oluşturmayı destekler
* Alınan ve gönderilen faksları görüntüleme
* Telefon Defteri (Girdiler isteğe bağlı olarak SQL veritabanı veya LDAP dizininden okunabilir)
* Bir iletişimde görünür tablo sütunlarını seçebilme
* On Dil desteği: Almanca, Çince, Fransızca, İngilizce, İtalyanca, Rusça, İspanyolca, Polonya dili, Türkçe ve Yunanca 

Web sitesi: http://www.yajhfc.de/
e-posta:    Jonas Wolz <info@yajhfc.de>

KURULUM
------------

Gereksinimler:
* JRE 5.0 yada üstü (Java 6 tavsiye edilir)
* Ağınızda çalışır durumda bir HylaFAX sunucusu (elbette ;-) )
* TIFF dosyalarını görebilmek için yazılım ( SSS'ye bakaniz )
* İsteğe bağlı: PostScript görüntüleyici (örn. Ghostview), GhostScript
     ve tiff2pdf (libtiff-utils'den)

Kurulum için sadece YajHFC'yi dosya sisteminizdeki herhangi bir dizine
indirin.
Çalıştırmak için "java -jar yajhfc-0_5_1.jar" kullanın.
(Windows'da çalıştırmak için genellikle jar dosyasına iki kez tıklayabilirsiniz)

LİSANS
-------

YajHFC GPL altında özgür yazılım lisanslıdır.
ayrıntılar için COPYING dosyasına bakınız.

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

YajHFC bir ilkten daha karmaşık ("merhaba dünya" programlarından daha gelişmiş) 
Java projesi olarak Eclipse IDE kullanılarak özgün biçimde yazılmıştır.
( Ve bundan sonra çok yol katedildi... ;-) )


DEĞİŞİKLİKLER
-------

0.5.1:
- Salt Uçbirim eklentisi
- Web sitemiz "yajhfc.berlios.de" yerine "www.yajhfc.de" oldu
- Bazı yazılım hataları düzeltildi ve ufak özellikler eklendi

0.5.0:
- Faks listelerini yerelde saklama desteği, muhtemelen uygulama açılışının 
çok daha hızlı olduğunu hissettirecektir
- HylaFAX sunucuyu (ve HylaFAX kullanıcı kimlik doğrulamasını) atlayarak 
doğrudan recvq ve doneq dizininden okuma desteği (deneysel)
Değişen dosyalar yenilenmiş faks listesinden okunacağından büyük kuyruğa
sahip sunuculardaki işlem yükün azaltılmasına öncülük edebilir. Bununla birlikte, hala
tecrübe etmek gerekli. (Bu özellikle ilgili herhangi bir geri dönüş, çok büyük 
bir memnuniyete neden olur)
- Seçenekler iletişimi öncekinden daha hızlı açılabilir
- Seçenekler iletişiminde "Bağlantı kontrolü" düğmesi
- Çoklu sunucu desteği
- Çoklu kimlik desteği
- Faks listeleri CSV, HTML veya XML biçiminde kaydedilebilir
- Ayarlanabilir klavye kısayol desteği

0.4.4:
- Gelişmiş MAC OS desteği (genelde estetik değişiklikler)
- Özelleştirilmiş dosya çeviricisi tanımlama desteği
- Kullanıcı arabiriminden (UI) gelişmiş ayarlara erişim
- Telefon defteri yazdırma desteği
- Geliştirilmiş faks yazdırma
- HTML kapak sayfasında @@CCNameAndFax@@ etiket desteği
- Çoklu alıcılar için faksı tekrar gönderme
- Günlüğü canlı görmek için günlük uçbirimi
- Fakslar için gönderi ve görünüm biçimi ayrımı
- Kullanıcı-düzenleyebilir modem listesi
- Yeni "geçersiz kılma-ayarı" komut satırı parametresi
- Birkaç hata düzeltildi


0.4.3:
- Çince çeviri eklendi
- Gönderilmiş fakslar için "günlük görüntüleme" özelliği
- Tepsi uyarı mesajlarını kapatabilme
- Telefon defteri öğeleri için filtre
- "Genişletilmiş çözünürlük" desteği (örn: USEXVRES=yes)
- Yeni komut satırı parametresi: --modem
- TCP/IP ve adlandırılmış iletim yolu (named pipe) sanal yazıcı bağlantı noktası desteği
- /etc/yajhfc'deki ayarlar için varsayılan/geçersiz-kıl ayar desteği
- Bir kaç hata düzeltildi
- YajHFC için RPM ve DEB paketleri artık mevcut


0.4.2a:
Önceden ayar dosyası mevcut olmadığında, ayaların kaydedilmemesine neden olan hata düzeltildi.

0.4.2:
- Polonya dili desteği eklendi
- Alıcıların, metin dosyasından okunması desteği
- Arzu edilen pencere durumunu komut satırından tanımlayabilme
- Ana pencereyi göstermeden "Salt gönderme kipi" (--background 
  veya --noclose tanımlamadan belge gönderirken)
- Yeni faks işlemi için isteğe bağlı HylaFAX seçenekleri tanımlayabilme
- Windows 7/Vista bazen de XP'de program çalışırken kullanıcı oturumu kapattığında
  YajHFC'nin ayarları kaydetmeme sorununa neden olan Java hatasına çözüm.
- ISO-8859-1 olmayan dil kodlamasında oluşacak sorunlardan kaçınmak için varsayılan kapak
  sayfası artık HTML
- Windows için kur yazılımı artık isteğe bağlı olarak tiff2pdf ve GhostScript kuruyor
- Çeşitli yazılım hataları giderildi, iyileştirmeler ve kaynak kodunda temizlemeler yapıldı

0.4.1:
- Telefon defterinde dağıtım listesi desteği (sadece XML+JDB)
- CSV telefon defteri desteği ( örn: içe/dışa aktarma)
- Gönderilmiş fakslar için "arşiv bayrağı" (doneop) desteği
- Ana penceredeki fakslar için hızlı arama çubuğu
- Bazı hatalar düzeltildi

0.4.0:
- Yunanca çeviri eklendi
- Faks aramasına el ile cevap verme desteği 
- Güncelleme kontrolü
- Arşiv dizin desteği
- Tek dosya olarak faksları görüntüleme ve gönderme (PDF, TIFF veya PS biçiminde)
- Telefon defteri şimdi bir çok ortak alan içeriyor
- Java 6 altında istem simgesi desteği
- Geliştirilmiş seçenekler iletişimi
- Geliştirilmiş komut satırı desteği (YajHFC şimdi
   kullanıcı onayı olmadan faks göndermek için kullanılabilir)
- Parolalar için bazı basit gizlemeler
- Bir çok iç kod temizliği

0.3.9:
- İtalyanca ve Türkçe çeviri eklendi 
- Telefon defteri için "Hızlı arama"
- Bir çok hata düzeltildi ve küçük geliştirmeler yapıldı

0.3.8a:
- XML telefon defteri hatası giderildi.
- Kullanıcı Arayüzünde ufak geliştirmeler yapıldı

0.3.8:
- Çoklu telefon defteri desteği için gelişmiş telefon defteri penceresi 
- Faks okundu/okunmadı durumunu merkezi veritabanı tablosuna kaydetme desteği
- JDBC sürücüleri ve eklentiler eklemek için görsel panel
- YajHFC Rusça çevirisi eklendi.

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
Çeviri yapan için (belgelendirmeyi çevirmeden) yeni çeviri oluşturmak için ön çalışmaya
bir kaç saat ve her yeni sürüme yaklaşık bir saat ayırmak gerekir.
Bunu yapmak için "İleri" seviye teknik bilgiye ihtiyacınız yok (sadece bir
metin düzenleyici ile nasıl çalışacağınızı bilmeniz yeterli,
ki özel yazılım kurarak çok daha kolaylaşır).

Kısaca, eğer ana dilinize çevirilmiş bir YajHFC isterseniz, gönüllülerin daima başımın üstünde
yeri var. ;-)
