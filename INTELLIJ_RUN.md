# How to open and run in IntelliJ IDEA

1. Open IntelliJ IDEA.
2. Choose **Open** and select the `DAA_Assignment2` folder.
3. IntelliJ should detect `pom.xml` and import the Maven project.
4. Make sure Project SDK is Java 17 or newer.
5. Run `DataStructuresTest` from `src/test/java/daa/DataStructuresTest.java` using the green Run icon.
6. To run the benchmark, open `src/main/java/daa/Main.java` and press Run.
7. Keep the working directory set to the project root (`DAA_Assignment2`), because the benchmark writes relative paths under `results/`.
8. After the benchmark finishes, open `results/results.csv`.
9. The four final charts are in `results/plots/`: `w1.png`, `w2.png`, `w3.png`, and `w4.png`.

## Command line

From the project root:

```bash
mvn clean test
mvn -q -DskipTests package && java -jar target/DAA_Assignment2-1.0.jar
```
