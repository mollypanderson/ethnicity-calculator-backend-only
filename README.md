# Ethnicity Calculator

### Getting Started
```
git clone git@github.com:mollypanderson/ethnicity-calculator.git
./gradlew build
./gradlew run path/to/your/gedcom/file
```

<h3>To export GEDCOM</h3>
Ancestry
1. Open your tree
2. In the left-hand toolbar, click the 3 dots "More" -> Tree Settings
3. Right-hand side -> Manage Your Tree -> Export Tree
4. When it's done, click Download

<h3>To run the tool locally on your computer</h3>
IntelliJ:
1. Open project in IntelliJ
2. Be sure to use Java 17
3. Under Run Configurations, add the path to the GEDCOM file you exported as a CLI argument. Example: `src/main/resources/anderson-family-tree-6-10-24.ged`
4. Click 'Run'

