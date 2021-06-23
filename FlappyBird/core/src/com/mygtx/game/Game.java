package com.mygtx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Random;

import javax.swing.JTextArea;

public class Game extends ApplicationAdapter {

	//vars para as texturas serem renderizadas
	private  SpriteBatch batch;
	private  Texture[] passaros;
	private Texture fundo;
	private  Texture canobaixo;
	private  Texture canotopo;
	private Texture gameOver;

	//conta pontos e altera o estado do jogo
	private int pontos = 0;
	private int pontuacaomaxima = 0;
	private int estadoJogo = 0;

	//var para movimentacao
	private int movimentaY = 0;
	private  int movimentaX = 0;

	//random usado nos canos
	private  Random random;

	//var usados para setar elemenos na tela
	private float larguraDispositivo;
	private  float alturaDispositivo;
	private  float espacoentrecanos;
	private  float posicaoCanohorizontal;
	private  float posicaocanovertical;
	private  float posicaoInicialVerticalPassaro = 0;
	private float posicaoHorizontalPassaro;


	//var usados na fisica
	private  float variacao = 0;
	private  int gravidade = 0;

	//textos
	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	BitmapFont textoMelhorPontuacao;

	Preferences preferencias;

	private  boolean passouCano = false;

	//renderiza os colisores
	private ShapeRenderer shapeRenderer;
	//colisores
	private Circle circulopassaro;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;

	//sons
	Sound somvoando;
	Sound somcolisao;
	Sound sompontuacao;

	@Override
	public void create () {
		//instancia os objetos para a tela
		inicializaImagens();
		inicializaTela();
	}

	@Override
	//renderiza os objetos na aplicação
	public void render () {

		gameplay();
		validarPontos();
		desenhaImagens();
		detectarColisao();
	}


	@Override
	public void dispose() {

	}

	private void inicializaImagens() {


		//guardando as texturas nas var
		passaros = new Texture[3];
		passaros[0] = new Texture("passaro1.png");
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");

		canotopo = new Texture("cano_topo_maior.png");
		canobaixo = new Texture("cano_baixo_maior.png");

		//instancia background
		fundo = new Texture("fundo.png");

		//instancia o sprite de game over
		gameOver = new Texture("game_over.png");

	}

	private void inicializaTela() {

		batch = new SpriteBatch();

		//setando a var random
		random = new Random();

		//guardando as infos da resolução do aparelho em variaveis para definir o tamanho das texturas
		larguraDispositivo = Gdx.graphics.getWidth();
		alturaDispositivo = Gdx.graphics.getHeight();
		//definindo a posicao inicial do passaro
		posicaoInicialVerticalPassaro = alturaDispositivo / 2;
		posicaoCanohorizontal = larguraDispositivo;
		espacoentrecanos = 350;

		//setando o texto como bitmap
		textoPontuacao = new BitmapFont();
		//define a cor do texto
		textoPontuacao.setColor(Color.WHITE);
		//seta o tamanho do texto
		textoPontuacao.getData().setScale(10);

		//setando o texto como bitmap
		textoMelhorPontuacao = new BitmapFont();
		//define a cor do texto
		textoMelhorPontuacao.setColor(Color.RED);
		//seta o tamanho do texto
		textoMelhorPontuacao.getData().setScale(2);

		//setando o texto como bitmap
		textoReiniciar = new BitmapFont();
		//define a cor do texto
		textoReiniciar.setColor(Color.GREEN);
		//seta o tamanho do texto
		textoReiniciar.getData().setScale(2);

		//incializa o render dos colliders
		shapeRenderer = new ShapeRenderer();
		//inicializa os colliders
		circulopassaro = new Circle();
		retanguloCanoCima = new Rectangle();
		retanguloCanoBaixo = new Rectangle();

		//colocando os audios nas var
		somcolisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		somvoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		sompontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

		//colocando as pref na var
		preferencias = Gdx.app.getPreferences("flappybird");
		pontuacaomaxima = preferencias.getInteger("pontuacaoMaxima", 0);
	}


	private void gameplay() {

		//pega o toque na tela
		boolean toqueTela = Gdx.input.justTouched();

		//verifica o estado do jogo
		if(estadoJogo == 0)   {

			//passaro vai pra cima ao clicar
			if (Gdx.input.justTouched()) {
				gravidade = -15;
				estadoJogo = 1;
				somvoando.play();
			}

			//verifica o estado do jogo
		} else if (estadoJogo == 1){

			//passaro vai pra cima ao clicar
			if (Gdx.input.justTouched()) {
				gravidade = -15;
				somvoando.play();
			}

			//cano vindo na direção do player
			posicaoCanohorizontal -= Gdx.graphics.getDeltaTime() * 200;

			if(posicaoCanohorizontal < - canobaixo.getWidth()){
				posicaoCanohorizontal = larguraDispositivo;
				//faz o random da posicao dos canos
				posicaocanovertical = random.nextInt(400) -200;
				//deixa o passoucano false
				passouCano = false;
			}

			//conecta o toque na tela com a gravidade
			if (posicaoInicialVerticalPassaro > 0 || toqueTela)
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;

			gravidade++;   //incrementa a gravidade.

			//quando o estado do jogo estiver 2
		} else if (estadoJogo == 2){
			if (pontos > pontuacaomaxima){
				pontuacaomaxima = pontos;
				preferencias.putInteger("pontiacaomaxima", pontuacaomaxima);
			}

			//passaro volta pra tras ao bater
			posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime() * 500;

			if(toqueTela) {
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoHorizontalPassaro = 0;
				posicaoInicialVerticalPassaro = 0;
				posicaoInicialVerticalPassaro = alturaDispositivo/2;
				posicaoCanohorizontal = larguraDispositivo;

			}

		}


	}

	private void desenhaImagens() {
		//começa a execução
		batch.begin();
		//renderiza o fundo encaixando com o tamanho do dispositivo
		batch.draw(fundo,0,0,larguraDispositivo,alturaDispositivo);
		//renderiza o passaro na tela
		batch.draw(passaros[(int) variacao],50 + posicaoHorizontalPassaro, posicaoInicialVerticalPassaro);

		//instancia o cano na tela e o espaçamento entre eles
		batch.draw( canobaixo, posicaoCanohorizontal  , alturaDispositivo/2 - canobaixo.getHeight() - espacoentrecanos/2 + posicaocanovertical);
		batch.draw( canotopo, posicaoCanohorizontal  ,alturaDispositivo/2 + espacoentrecanos / 2 + posicaocanovertical);

		textoPontuacao.draw(batch, String.valueOf(pontos), larguraDispositivo / 2, alturaDispositivo - 100);

		//se o estado for 2
		if(estadoJogo == 2){
			//desenha a imagem de game over
			batch.draw(gameOver, larguraDispositivo / 2 +200 - gameOver.getWidth(), alturaDispositivo / 2);
			//escreve o comando e reiniciar
			textoReiniciar.draw(batch, "TOQUE NA TELA PARA REINICIAR!", larguraDispositivo / 2 -250, alturaDispositivo / 2 - gameOver.getHeight() / 2);
			//escreve a melhor pontuacao
			textoMelhorPontuacao.draw(batch, "SUA MELHOR PONTUAÇÃO É:" + pontuacaomaxima + "PONTOS", larguraDispositivo / 2 -250, alturaDispositivo / 2 - gameOver.getHeight() * 2);
		}

		//para a execucao
		batch.end();
	}

	private void validarPontos() {
		//condicao de detectar ao passar do cano
		if(posicaoCanohorizontal < 50 - passaros[0].getWidth()){
			//vendo se passou do cano
			if(!passouCano){
				//adiciona pontos ao passar do cano
				pontos++;
				//se passar do cano fica como true
				passouCano = true;
				//toca o som de pontuacao
				sompontuacao.play();
			}
		}

		//muda a animacao
		variacao += Gdx.graphics.getDeltaTime() * 10;
		if (variacao > 3)
			variacao = 0;
	}

	private void detectarColisao() {

		//setando colisores
		circulopassaro.set(50 + passaros[0].getWidth() / 2, posicaoInicialVerticalPassaro + passaros[0].getHeight() / 2, passaros[0].getWidth() / 2);

		//fazendo os colliders nos canos
		retanguloCanoBaixo.set(posicaoCanohorizontal, alturaDispositivo / 2 - canobaixo.getHeight() - espacoentrecanos / 2 + posicaocanovertical, canobaixo.getWidth(), canobaixo.getHeight());
		retanguloCanoCima.set(posicaoCanohorizontal, alturaDispositivo / 2 + espacoentrecanos / 2 + posicaocanovertical, canotopo.getWidth(), canotopo.getHeight() );


		//detecta a colisao
		boolean colisaoCanoCima = Intersector.overlaps(circulopassaro, retanguloCanoCima);
		boolean colisaoCanoBaixo = Intersector.overlaps(circulopassaro, retanguloCanoBaixo);

		//avisa se colidiu
		if (colisaoCanoBaixo || colisaoCanoCima)
		{
			if (estadoJogo == 1)
			{
				//da play no som de colisao
				somcolisao.play();
				estadoJogo = 2;
			}
		}

	}


}
