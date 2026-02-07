package br.com.automacao.base;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BasePage {

	protected WebDriver driver;
	private final int TIMEOUT = 5;

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
}
