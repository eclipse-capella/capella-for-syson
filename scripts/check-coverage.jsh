double checkCoverage(String module) {
  double coverage = 0.0;

  var totalStart = "<td>Total</td>";
  var resultStart = "<td class=\"ctr2\">";
  var resultEnd = "%</td>";

  var modulePath = module;
  if (!module.isBlank()) {
    modulePath += "/";
  }
  try {
    var path = Paths.get("backend/releng/capella-test-coverage/target/site/jacoco-aggregate/" + modulePath + "index.html");
    var optionalLine = Files.readAllLines(path).stream().filter(line -> line.contains(totalStart)).findFirst();
    if (optionalLine.isPresent()) {
      var line = optionalLine.get();
      var totalIndex = line.indexOf(totalStart);
      var startIndex = line.indexOf(resultStart, totalIndex);
      var endIndex = line.indexOf(resultEnd, startIndex);
      var result = line.substring(startIndex + resultStart.length(), endIndex);
      coverage = Double.parseDouble(result.replaceAll("\\p{Z}", "").trim());
    }
  } catch (IOException exception) {
    System.out.println(exception.getMessage());
  }
  return coverage;
}

record ModuleCoverage(String moduleName, double expectedCoverage) {}
var moduleCoverageData = List.of(
  new ModuleCoverage("capella-application", 37.0),
  new ModuleCoverage("capella-application-configuration", 32.0),
  new ModuleCoverage("capella-model-services", 15.0),
  new ModuleCoverage("capella-diagram-common-view", 100.0),
  new ModuleCoverage("capella-diagram-lab-view", 94.0),
  new ModuleCoverage("capella-diagram-oab-view", 98.0),
  new ModuleCoverage("capella-diagram-sab-view", 91.0),
  new ModuleCoverage("capella-ddv-view", 67.0),
  new ModuleCoverage("capella-table-view", 26.0),
  new ModuleCoverage("capella-form-view", 37.0)
);

void display(String module, double coverage, double expected) {
  var status = coverage < expected ? "more tests are required" : "OK";
  System.out.format("%-45s%10.2f%10.2f%35s%n", module, coverage, expected, status);
}

System.out.format("%-45s%10s%10s%35s%n", "Module", "Coverage", "Expected", "Status");

double global = checkCoverage("");
double expectedGlobalCoverage = 53.0;
boolean isValidCoverage = global >= expectedGlobalCoverage;
display("total", global, expectedGlobalCoverage);

for (var moduleCoverage: moduleCoverageData) {
  var coverage = checkCoverage(moduleCoverage.moduleName());
  display(moduleCoverage.moduleName(), coverage, moduleCoverage.expectedCoverage());
  isValidCoverage = isValidCoverage && coverage >= moduleCoverage.expectedCoverage();
}

/exit isValidCoverage ? 0 : 1
