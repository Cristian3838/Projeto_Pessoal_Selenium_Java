package br.com.automacao.base;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BasePage {

	protected WebDriver driver;
	private final int TIMEOUT = 20;

	public BasePage() {
		this.driver = BaseTest.getDriver();
	}

	protected WebElement esperarElemento(By by) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));
		return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
	}

	protected void escrever(By by, String texto) {
		WebElement elemento = esperarElemento(by);
		elemento.clear();
		elemento.sendKeys(texto);
	}

	protected void clicaNoElemento(By by) {
		driver.findElement(by).click();
	}

	protected String retornaTexto(By by) {
		return esperarElemento(by).getText();
	}
	
	public void aguardarPreloaderSumi() {
	    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
	    // Espera até que o preloader não esteja mais visível ou presente
	    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("preloader")));
	}
	
	public void aguardarCarregamento() {
	    // Definimos uma espera de até 15 segundos
	    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
	    
	    // Instruímos o Selenium a esperar até que o elemento com a classe 'preloader'
	    // (que causou o erro de interceptação) desapareça da tela.
	    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("preloader")));
	}
}
