# Tour de Lost

เกม Java 2D สำหรับเรียนรู้ Graph Algorithms ผ่านการทำภารกิจด้วยตัวเอง ขอบเขตเป้าหมาย 15 ด่าน แต่ละด่านมีเมืองหรือประเทศตามแบบที่ทีมกำหนด ผู้เล่นเลือกโหนด เส้นทาง และกิจกรรมเอง หลังผ่านด่านจึงเปิดบทเรียนและการสาธิตอัลกอริทึม

## สถานะปัจจุบัน

เป็นโครงเริ่มต้นที่คอมไพล์ได้และเปิดหน้าต่าง Swing ได้ โฟลเดอร์ของแต่ละระบบเตรียมไว้แล้ว ยังไม่มีระบบเดิน ภารกิจ อัลกอริทึม หรือด่านที่เล่นได้

## เครื่องมือ

- JDK 21 หรือใหม่กว่า สคริปต์คอมไพล์ด้วย `--release 21` จึงได้ไฟล์ที่รันบน Java 21 ขึ้นไป
- VS Code และ Extension Pack for Java หากต้องการ Run/Debug ใน editor
- PowerShell สำหรับสคริปต์ Build/Run บน Windows
- ใช้ Swing + Java2D ใน JDK ไม่มีไลบรารีภายนอกที่ต้องดาวน์โหลด

ควรตกลง JDK รุ่นเดียวกันในทีม เครื่องที่ใช้เตรียมโครงนี้มี JDK 26 และคอมไพล์เป็นเป้าหมาย Java 21

## Build และ Run

เปิด Terminal ในโฟลเดอร์โปรเจกต์:

```powershell
# คอมไพล์
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build.ps1

# คอมไพล์แล้วเปิดเกม
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\run.ps1
```

ใน VS Code:

1. เปิดโฟลเดอร์โปรเจกต์นี้ทั้งโฟลเดอร์
2. กด `Ctrl+Shift+B` เพื่อ Build
3. เลือก `Terminal > Run Task > Tour de Lost: Run` เพื่อรัน
4. หรือกด `F5` แล้วเลือก `Tour de Lost` เพื่อ Debug

สคริปต์หา JDK จาก `JAVA_HOME`, PATH ที่ชี้ไปยัง JDK จริง และโฟลเดอร์ติดตั้งทั่วไป โดยข้าม Oracle `javapath` launcher shims หากหาไม่พบ ให้ตั้ง `JAVA_HOME` ไปยังโฟลเดอร์ JDK เช่น `C:\Program Files\Java\jdk-26` โดยไม่ต่อท้าย `bin`

หาก Run Task ใช้งานได้ แต่ F5 หา JDK ไม่พบ ให้เลือก JDK ในคำสั่ง `Java: Configure Java Runtime` ของ VS Code อย่า commit path เครื่องส่วนตัวลงไฟล์ตั้งค่าส่วนกลาง

## โครงสร้างและเจ้าของงาน

```text
src/tourdelost/
  Main.java       จุดเริ่มโปรแกรมและการเชื่อมระบบ
  world/          Pai: กราฟ ผู้เล่น และการเดิน
  ui/             Dream: หน้าจอ เมาส์ แอนิเมชัน และธีมเมือง
  game/           Book: ด่าน ทรัพยากร กิจกรรม และสถานะเกม
    mission/      Book: กติกาและผลภารกิจ
  algorithm/      Korn: อัลกอริทึม ผลอ้างอิง และขั้นตอนสาธิต
  lesson/         Korn: เนื้อหาและการควบคุมบทเรียน
resources/        ภาพ เสียง ข้อมูลด่าน และบทเรียน
scripts/          Build/Run บน Windows
docs/             กติกาเกมและข้อตกลงของทีม
bin/              ผลคอมไพล์ ไม่เก็บใน Git
```

ในโฟลเดอร์ที่ยังไม่มี implementation ใช้ `package-info.java` อธิบายขอบเขตงาน ไม่ได้สร้างคลาสเปล่าให้ดูเหมือนระบบทำงานแล้ว

เมื่อเพิ่มไฟล์ข้อมูล ให้ใช้ UTF-8 และอ่าน resource ผ่าน classpath เช่น `Main.class.getResource("/images/player/hero.png")` สคริปต์และ launch configuration ใส่ `resources/` ใน classpath แล้ว หากอ่าน `.properties` ที่มีภาษาไทย ให้ใช้ Reader ที่กำหนด UTF-8

## เริ่มงานร่วมกัน

1. ยืนยันรายละเอียดทั้ง 15 ด่านและข้อมูลของด่านตัวอย่าง
2. ตกลงข้อมูลและเมธอดร่วมกันตาม `docs/interfaces.md`
3. ทำงานในโฟลเดอร์ที่แต่ละคนรับผิดชอบ และส่งโครงเมธอดให้ทีมก่อน implementation เสร็จ
4. รวมให้เล่นด่านตัวอย่างครบวงจรตั้งแต่ Week 1
5. Week 2 ทยอยเชื่อมและตรวจทีละ 1–3 ด่านจนครบ 15 ด่าน

ยังไม่ได้ตั้ง remote repository หรือสร้าง commit สามารถเชื่อม GitHub/Git remote ของทีมภายหลังได้
