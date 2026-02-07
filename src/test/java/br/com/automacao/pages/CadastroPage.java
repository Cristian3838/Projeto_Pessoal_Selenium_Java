package br.com.automacao.pages;

import org.openqa.selenium.By;

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

}
