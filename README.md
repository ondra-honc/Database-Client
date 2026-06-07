# Database Client

Moderní, lehký a rychlý desktopový klient pro správu SQLite databází postavený na frameworku JavaFX. Aplikace nabízí plně responzivní tmavé uživatelské rozhraní inspirované moderními vývojářskými nástroji.

## Funkce

- **Welcome Page:** Přehledná úvodní obrazovka s rychlými volbami pro vytvoření nebo otevření databáze.
- **Správa databází:** Kompletní tvorba nových `.db` souborů přímo z aplikace včetně výběru lokace.
- **Správa tabulek:**
  - Vytváření nových tabulek s definicí primárního klíče.
  - Podpora pro datové typy (`INTEGER`, `TEXT`, `BIGINT`).
  - Volitelná podpora pro `AUTOINCREMENT` (inteligentně vázaná na typ INTEGER).
  - Bezpečné mazání tabulek (`DROP TABLE`) s potvrzovacím dialogem.
- **Robustní Error Log:** Systém odchycení neočekávaných výjimek, který uživatele neobtěžuje složitým stack trace, ale ukládá detailní logy do `logs/log.txt`.

## UI/UX

- **Modern Dark Theme:** Vizuální styl s tyrkysovými akcenty (`#00adb5`).
- **Pixel-Perfect layout:** Sjednocené výšky a vnitřní paddingy u `ComboBox` a `TextField` prvků.
- **Okamžité Tooltipy:** Uživatelské nápovědy u ovládacích prvků s ultra rychlou odezvou (100 ms) stylované do tmavého schématu.

## Technologie

- **Java 17** (nebo novější)
- **JavaFX 17+** (Graphics, Controls, FXML)
- **SQLite JDBC Driver** (pro komunikaci s databází)
- **CSS3** (pro kompletní stylování vestavěných komponent)

## Instalace a Spuštění

### Požadavky
Ujistěte se, že máte nainstalované JDK (verze 17 nebo novější) a nakonfigurovaný Maven/Gradle.

### Klonování projektu
```bash
git clone [https://github.com/vyskocil-ondra/database-client.git](https://github.com/vyskocil-ondra/database-client.git)
```

### Spuštění Mavenu
```bash
cd database-client
mvn clean javafx:run
```

## 📂 Struktura Projektu

```text
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── ondra/
│       │           ├── App.java       
│       │       
│       └── resources/
│           ├── style.css                
│           └── database.png            
└── logs/
    └── log.txt  
```                                          