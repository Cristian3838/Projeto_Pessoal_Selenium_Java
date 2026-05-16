package br.com.automacao.pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import br.com.automacao.base.BasePage;

public class CadastroPage extends BasePage {

	public void botãoNovoUsuario() {
		clicaNoElemento(By.xpath("//a[@href='/cadastro' and text()='Novo usuário?']"));
	}

	public void escreverTextoNome(String texto) {
		escrever(By.xpath("//input[@placeholder='Nome']"), texto);
	}

	public void escreverTextoEmail(String texto) {
		escrever(By.xpath("//input[@placeholder='Email']"), texto);
	}

	public void escreverTextoSenha(String texto) {
		escrever(By.xpath("//input[@placeholder='Password']"), texto); 
	}

	public void botaoCadastrar() {
		clicaNoElemento(By.xpath("//input[@value='Cadastrar']"));
	}

	public void BotaoLogar() {
		clicaNoElemento(By.xpath("//button[text()='Entrar']"));
	}

	public String obterMensagemAlerta() {
		return retornaTexto(By.cssSelector("div[role='alert']"));
	}
	
	public String obterMensagemAlertaCarga() {
		return retornaTexto(By.xpath("//p[contains(., 'Carga já finalizada.')]"));
	}

	public void escreverTextoNome2(String texto) {
		escrever(By.xpath("//input[@placeholder='Matrícula']"), texto);
	}

	public void escreverTextoSenha2(String texto) {
		escrever(By.xpath("//input[@placeholder='Senha']"), texto);
	}

	public void botaoAcessar() {
		clicaNoElemento(By.xpath("//span[text()='ACESSAR']"));
	}

	public void comboAgencia() {
		By setaAgencia = By
				.xpath("//span[contains(., '002 - ABAETETUBA')]//i[contains(@class, 'agencia-icone-select')]");
		WebElement elemento = esperarElemento(setaAgencia);

		// Clique via JS para evitar qualquer bloqueio residual
		((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", elemento);
	}

	public void escolheAgencia() {

		By setaAgencia = By.xpath("//button[@role='menuitem' and contains(., '007 - BELCEN')]");
		WebElement elemento = esperarElemento(setaAgencia);

		// Clique via JS para evitar qualquer bloqueio residual
		((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", elemento);

	}

	public void finalizaCarga() {

		By setaAgencia = By.xpath("//span[text()='Finaliza Cargas']");
		WebElement elemento = esperarElemento(setaAgencia);

		// Clique via JS para evitar qualquer bloqueio residual
		((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", elemento);

	}

	public void primeiraModalConfirmacaoCarga() {
	    // 1. Espera o loader interno sumir completamente
	    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
	    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[contains(text(), 'Aguarde')]")));

	    // 2. Localiza o botão SIM
	    By btnSim = By.xpath("//button[contains(., 'SIM')]");
	    
	    // 3. Loop de Tentativa: Clica e verifica se a modal sumiu
	    // Isso resolve o problema do clique "perdido" durante o re-render do Angular
	    boolean modalAindaVisivel = true;
	    int tentativas = 0;

	    while (modalAindaVisivel && tentativas < 2) {
	        try {
	            WebElement elemento = wait.until(ExpectedConditions.elementToBeClickable(btnSim));
	            
	            // Tenta o clique via JS para forçar a execução
	            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", elemento);
	            
	            // Pequena pausa para o sistema processar o clique
	            Thread.sleep(1000); 
	            
	            // Verifica se o botão ainda está lá. Se não estiver, a modal fechou!
	            modalAindaVisivel = driver.findElements(btnSim).size() > 0;
	        } catch (Exception e) {
	            modalAindaVisivel = false; // Se der erro, assume que o elemento sumiu
	        }
	        tentativas++;
	    }
	}
	
	public void segundaModalConfirmacaoCarga() {

	    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(40));

	    // Espera nova modal aparecer
	    WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
	            By.cssSelector("mat-dialog-container, .cdk-overlay-pane")
	    ));

	    // Espera botão SIM da segunda modal
	    WebElement btnSim = wait.until(ExpectedConditions.elementToBeClickable(
	            By.xpath("//mat-dialog-container//button[contains(.,'SIM')] | //div[contains(@class,'cdk-overlay-pane')]//button[contains(.,'SIM')]")
	    ));

	    ((org.openqa.selenium.JavascriptExecutor) driver)
	            .executeScript("arguments[0].click();", btnSim);

	    // Espera mensagem final (sucesso ou erro)
	    wait.until(ExpectedConditions.or(

	            ExpectedConditions.visibilityOfElementLocated(
	                    By.xpath("//*[contains(text(),'Carga já finalizada')]")
	            ),

	            ExpectedConditions.visibilityOfElementLocated(
	                    By.xpath("//*[contains(text(),'Carga finalizada')]")
	            ),

	            ExpectedConditions.invisibilityOf(modal)
	    ));
	}
	
	
}
