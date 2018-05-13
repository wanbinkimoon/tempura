import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.*; 
import ddf.minim.analysis.*; 
import controlP5.*; 

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

// ================================================================

public void settings(){ 
  size(stageW, stageH);
}

// ================================================================

public void setup() {
  spectrum = createGraphics(width, (stageM + ( stageM / 2 )) + audioMax + (rectS * 4) + (valuePadding * 6));
  rain = createGraphics(width, height - (stageM + ( stageM / 2 )) + audioMax + (rectS * 4) + (valuePadding * 6));

  minim = new Minim(this);
  audio = minim.getLineIn(Minim.STEREO);

  audioFFT = new FFT(audio.bufferSize(), audio.sampleRate());
  audioFFT.linAverages(audioRange);

  // audioFFT.window(FFT.NONE);
  audioFFT.window(FFT.GAUSS);

  spectrum.beginDraw();
    controllersRender(spectrum, 0, 0);
  spectrum.endDraw();

  rain.beginDraw();
    rain.background(bgC);
    resetBtn(rain, 0, (stageM + ( stageM / 2 )) + audioMax + (rectS * 4) + (valuePadding * 6));
  rain.endDraw();
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

  // scene.stroke(#DD6600); 
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

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "build" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
