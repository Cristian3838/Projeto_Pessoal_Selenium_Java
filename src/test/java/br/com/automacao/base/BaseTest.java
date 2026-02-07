package br.com.automacao.base;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class BaseTest {
	protected static WebDriver driver;
	protected static ExtentReports extent;
	protected ExtentTest test;

	@BeforeAll
	public static void setupReport() {
		// Configura o local do relatório
		ExtentSparkReporter spark = new ExtentSparkReporter("target/RelatorioTestes.html");
		extent = new ExtentReports();
		extent.attachReporter(spark);
	}

	public static WebDriver getDriver() {
		if (driver == null) {
			iniciarDriver();
		}
		return driver;
	}

	private static void iniciarDriver() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-dev-shm-usage");
		options.addArguments("--remote-allow-origins=*");

		driver = new ChromeDriver(options);
		driver.manage().window().maximize();
	}

	@BeforeEach
	public void antesDeCadaTeste(TestInfo testInfo) {
		// Cria um log no relatório com o nome do método de teste
		test = extent.createTest(testInfo.getDisplayName());
		getDriver().get("https://seubarriga.wcaquino.me/login");
	}

	@AfterEach
	public void finalizar() {
		// Você pode adicionar lógica aqui para verificar se o teste falhou
		if (driver != null) {
			driver.quit(); // Descomente se quiser fechar o browser após cada teste
			driver = null;
		}
	}

	@AfterAll
	public static void tearDownReport() {
		// Salva o relatório final
		if (extent != null) {
			extent.flush();
		}
	}
}