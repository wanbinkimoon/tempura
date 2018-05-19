import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.*; 
import ddf.minim.analysis.*; 
import controlP5.*; 
import themidibus.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class build extends PApplet {

int bgC       = 20;
String dataPATH = "../../data/";

// ================================================================

PGraphics spectrum;
PGraphics rain;

// ================================================================

int     renderNum  = 0;
String  renderPATH = "../render/";

// ================================================================




 

// ================================================================

MidiBus myBus; 
Minim minim;
AudioInput audio;
FFT audioFFT;
ControlP5 sliders;
ControlP5 button;


// ================================================================

int ampVal;
int indexVal;
int stepVal;
int rangeVal;

// ================================================================

int audioRange  = 12;
int audioMax = 100;

float audioAmp = 125.0f;
float audioIndex = 0.05f;
float audioIndexAmp = audioIndex;
float audioIndexStep = 0.75f;

String settingsStr = "amplifier: " + audioAmp + " // index start: " + audioIndex +  " // index step: " + audioIndexStep;

int rectS       = 30;

// ================================================================

boolean analyze = false;
boolean colorize = false;

// ================================================================

int stageM      = 100;
int stageW      = (audioRange * rectS) + (stageM * 2);
int stageH      = 900;

// ================================================================

int xStart       = stageM;
int yStart       = stageM;
int xSpace       = rectS;

int valuePadding = 4;
int labelW       = 80;

int knobNumb = 16;
int[] knob = new int[knobNumb];
String knobTable;

// ================================================================

public void settings(){ 
  size(stageW, stageH);
}

// ================================================================

public void setup() {
  midiSetup();
  spectrum = createGraphics(width, (stageM + ( stageM / 2 )) + audioMax + (rectS * 4) + (valuePadding * 6));
  rain = createGraphics(width, height - (stageM + ( stageM / 2 )) + audioMax + (rectS * 4) + (valuePadding * 6));

  minim = new Minim(this);
  audio = minim.getLineIn(Minim.STEREO);

  audioFFT = new FFT(audio.bufferSize(), audio.sampleRate());
  audioFFT.linAverages(audioRange);

  audioFFT.window(FFT.NONE);
  // audioFFT.window(FFT.BARTLETT);
  // audioFFT.window(FFT.BARTLETTHANN);
  // audioFFT.window(FFT.BLACKMAN);
  // audioFFT.window(FFT.COSINE);
  // audioFFT.window(FFT.GAUSS);
  // audioFFT.window(FFT.HAMMING);
  // audioFFT.window(FFT.HANN);
  // audioFFT.window(FFT.LANCZOS);
  // audioFFT.window(FFT.TRIANGULAR);

  spectrum.beginDraw();
    controllersRender(spectrum, 0, 0);
  spectrum.endDraw();

  rain.beginDraw();
    rain.background(bgC);
    resetBtn(rain, 0, (stageM + ( stageM / 2 )) + audioMax + (rectS * 4) + (valuePadding * 6));
  rain.endDraw();
} 

// ================================================================

public void controllerChange(int channel, int number, int value) {  

  midiUpdate(channel, number, value);

  // Receive a controllerChange
  // println();
  // println("Controller Change:");
  // println("--------");
  // println("Channel:" + channel);
  // println("Number:" + number);
  // println("Value:" + value);
}

// ================================================================

public void controllersRender(PGraphics scene, int x, int y){
  // controllers
  sliders = new ControlP5(this);
  sliders
    .setGraphics(scene, x, y);

  sliders
    .addSlider("ampVal")
    .setPosition(stageM + labelW + valuePadding, stageM + audioMax + (valuePadding * 2) + rectS)
    .setSize((audioRange * rectS) - labelW - valuePadding, rectS)
    .setRange(0.0f, 600.0f)
    .setValue(25.0f)
    .setColorCaptionLabel(color(20));

  sliders
    .addSlider("indexVal")
    .setPosition(stageM + labelW + valuePadding, stageM + audioMax + (valuePadding * 3) + (rectS * 2))
    .setSize((audioRange * rectS) - labelW - valuePadding, rectS)
    .setRange(0, 100)
    .setValue(5)
    .setColorCaptionLabel(color(20));
  
  sliders
    .addSlider("stepVal")
    .setPosition(stageM + labelW + valuePadding, stageM + audioMax + (valuePadding * 4) + (rectS * 3))
    .setSize((audioRange * rectS) - labelW - valuePadding, rectS)
    .setRange(0, 1000)
    .setValue(250)
    .setColorCaptionLabel(color(20));
}

public void resetBtn(PGraphics scene, int x, int y){
  button = new ControlP5(this);
  button
    .setGraphics(scene, x, y)
    .addToggle("analyze")
    .setPosition(stageM, valuePadding)
    .setSize(rectS + (rectS / 3), rectS / 2)
    .setValue(true)
    // .setMode(ControlP5.BUTTON)
    .setColorCaptionLabel(color(200));

  button
    .setGraphics(scene, x, y)
    .addToggle("colorize")
    .setPosition(stageM + (rectS + (rectS / 3)) + valuePadding, valuePadding)
    .setSize(rectS + (rectS / 3), rectS / 2)
    .setValue(false)
    // .setMode(ControlP5.BUTTON)
    .setColorCaptionLabel(color(200));
}

// ================================================================

public void draw() {
  // linking controllers
  sliders
    .getController("ampVal")
    .setValue((int)map(knob[5], 0, 100, 0, 600));

  sliders
    .getController("indexVal")
    .setValue((int)map(knob[6], 0, 100, 0, 100));

  sliders
    .getController("stepVal")
    .setValue((int)map(knob[7], 0, 100, 0, 1000));


  audioAmp = ampVal;
  audioIndex = (float)indexVal / 1000;
  audioIndexStep = (float)stepVal / 1000;
  audioFFT.forward(audio.mix);


  spectrum.beginDraw();
  spectrum.background(bgC);
  topBarRender(spectrum);
  barsRender(spectrum);
  controllersLabelRender(spectrum); 
  spectrum.endDraw();

  rain.beginDraw();
    rainRender(rain);
    if (analyze) {
      rain.background(bgC);
      analyze = false;
    }
  rain.endDraw();
}

// ================================================================

public void topBarRender(PGraphics scene){
  // update display
  settingsStr = "amplifier: " + audioAmp + " // index start: " + audioIndex +  " // index step: " + audioIndexStep;

  // Print values
  scene.fill(75); scene.noStroke();
  scene.rect(stageM, stageM - (rectS + (valuePadding * 2)),(audioRange * rectS), rectS);

  scene.fill(0xff00AEFF);
  scene.textAlign(LEFT);
  scene.text(settingsStr, stageM + (rectS / 4), stageM - (rectS + (valuePadding * 2)) + (rectS  - (rectS / 3)));
}

public void barsRender(PGraphics scene){
  // Render stage
  scene.fill(45); scene.noStroke(); 
  scene.rect(stageM, stageM - (valuePadding / 2), audioRange * rectS, audioMax + valuePadding);

  for (int i = 0; i < audioRange; ++i) {

    float indexAvg = (audioFFT.getAvg(i) * audioAmp) * audioIndexAmp;
    float indexCon = constrain(indexAvg, 0, audioMax);

    // Render bars
    scene.noStroke();
    if (indexCon < 35) scene.fill(0xff00AE55, 255);
    else if (indexCon < 75) scene.fill(0xffFFD700, 255);
    else scene.fill(0xffFF6644, 255);
    
    scene.rect(xStart + (i * xSpace) + (valuePadding / 2), yStart + audioMax, rectS - valuePadding, - indexCon );
    // rect(xStart + (i * xSpace) + (valuePadding / 2), yStart, rectS - valuePadding, indexAvg );

    // Print values
    scene.fill(75); scene.noStroke();
    scene.rect(xStart + (i * xSpace) + (valuePadding / 2), stageM + audioMax + valuePadding + (valuePadding / 2), rectS - valuePadding, rectS - valuePadding );

    // fill(#00AEFF);
    if (indexCon < 35) scene.fill(0xff00AE55, 255);
    else if (indexCon < 75) scene.fill(0xffFFD700, 255);
    else scene.fill(0xffFF6644, 255);
    scene.textAlign(CENTER);
    scene.text(str((int)indexAvg), xStart + (i * xSpace) + (rectS / 2), stageM + audioMax + valuePadding + (rectS  - (rectS / 3)));

    audioIndexAmp += audioIndexStep;      
  }

  audioIndexAmp = audioIndex;
}

public void controllersLabelRender(PGraphics scene){
  // controller labels
  scene.fill(75); scene.noStroke();
  scene.rect(stageM, stageM + audioMax + (valuePadding * 2) + rectS, labelW, rectS);
  scene.fill(0xffF7F7F7); scene.textAlign(LEFT);
  scene.text("amplifier", stageM + (rectS / 4), stageM + audioMax + (valuePadding * 2) + rectS + (rectS  - (rectS / 3)));
  
  scene.fill(75); scene.noStroke();
  scene.rect(stageM, stageM + audioMax + (valuePadding * 3) + (rectS * 2), labelW, rectS);
  scene.fill(0xffF7F7F7); scene.textAlign(LEFT);
  scene.text("start", stageM + (rectS / 4), stageM + audioMax + (valuePadding * 3) + (rectS * 2) + (rectS  - (rectS / 3)));
  
  scene.fill(75); scene.noStroke();
  scene.rect(stageM, stageM + audioMax + (valuePadding * 4) + (rectS * 3), labelW, rectS);
  scene.fill(0xffF7F7F7); scene.textAlign(LEFT);
  scene.text("step", stageM + (rectS / 4), stageM + audioMax + (valuePadding * 4) + (rectS * 3) + (rectS  - (rectS / 3)));
}

public void rainRender(PGraphics scene){
  for (int i = 0; i < audioRange; ++i) {
    float indexAvg = (audioFFT.getAvg(i) * audioAmp) * audioIndexAmp;

    if (colorize) {
      scene.noFill();
      if ((indexAvg / 2) < (audioMax / 4)) scene.stroke(0xff00AE55, 50);
      else if ((indexAvg / 2) < (audioMax / 2)) scene.stroke(0xffFFD700, 50);
      else scene.stroke(0xffFF6644, 50);
    } else {
      scene.noStroke();
      scene.fill(0xffFF87B7, 50);
    }

    scene.rect(xStart + (i * xSpace) + (valuePadding / 2), rectS * 2, rectS - valuePadding, (indexAvg / 2));

    audioIndexAmp += audioIndexStep;  
  }

  scene.stroke(0xffF7F7F7); 
  scene.noFill();
  scene.line(stageM, (rectS * 2) + (audioMax / 2), width - stageM, (rectS * 2) + (audioMax / 2));

  audioIndexAmp = audioIndex;
}

// ================================================================

public void stop() {
  audio.close();
  minim.stop();
  super.stop();
}

// ================================================================

public void midiSetup(){
  MidiBus.list(); 
  myBus = new MidiBus(this, 0, 1);
}

public void midiUpdate(int channel, int number, int value){
	if(number == 21) knob[0] = (int)map(value, 0, 127, 0, 100);
	if(number == 22) knob[1] = (int)map(value, 0, 127, 0, 100);
	if(number == 23) knob[2] = (int)map(value, 0, 127, 0, 100);
	if(number == 24) knob[3] = (int)map(value, 0, 127, 0, 100);
	if(number == 25) knob[4] = (int)map(value, 0, 127, 0, 100);
	if(number == 26) knob[5] = (int)map(value, 0, 127, 0, 100);
	if(number == 27) knob[6] = (int)map(value, 0, 127, 0, 100);
	if(number == 28) knob[7] = (int)map(value, 0, 127, 0, 100);
	if(number == 41) knob[8] = (int)map(value, 0, 127, 0, 100);
	if(number == 42) knob[9] = (int)map(value, 0, 127, 0, 100);
	if(number == 43) knob[10] = (int)map(value, 0, 127, 0, 100);
	if(number == 44) knob[11] = (int)map(value, 0, 127, 0, 100);
	if(number == 45) knob[12] = (int)map(value, 0, 127, 0, 100);
	if(number == 46) knob[13] = (int)map(value, 0, 127, 0, 100);
	if(number == 47) knob[14] = (int)map(value, 0, 127, 0, 100);
	if(number == 48) knob[15] = (int)map(value, 0, 127, 0, 100);
}

public void midiMonitor(){
	knobTable = "\n\n_________________________________________________________________________________________________________________________________\n|  001  |  002  |  003  |  004  |  005  |  006  |  007  |  008  |  009  |  010  |  011  |  012  |  013  |  014  |  015  |  016  |\n|  "+ String.format("%03d", knob[0]) +"  |  "+ String.format("%03d", knob[1]) +"  |  "+ String.format("%03d", knob[2]) +"  |  "+ String.format("%03d", knob[3]) +"  |  "+ String.format("%03d", knob[4]) +"  |  "+ String.format("%03d", knob[5]) +"  |  "+ String.format("%03d", knob[6]) +"  |  "+ String.format("%03d", knob[7]) +"  |  "+ String.format("%03d", knob[8]) +"  |  "+ String.format("%03d", knob[9]) +"  |  "+ String.format("%03d", knob[10]) +"  |  "+ String.format("%03d", knob[11]) +"  |  "+ String.format("%03d", knob[12]) +"  |  "+ String.format("%03d", knob[13]) +"  |  "+ String.format("%03d", knob[14]) +"  |  "+ String.format("%03d", knob[15]) +"  |\n_________________________________________________________________________________________________________________________________";
	println(knobTable);
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "build" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
