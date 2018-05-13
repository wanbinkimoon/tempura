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

int bgC       = 10;
String dataPATH = "../../data/";

// ================================================================

int     renderNum  = 0;
String  renderPATH = "../render/";

// ================================================================





// ================================================================

Minim minim;
AudioInput audio;
FFT audioFFT;
ControlP5 ampCtrl;
ControlP5 indexCtrl;
ControlP5 stepCtrl;
ControlP5 rangeCtrl;

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

int stageM      = 100;
int stageW      = (audioRange * rectS) + (stageM * 2);
int stageH      = 700;

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
  background(bgC);

  minim = new Minim(this);
  audio = minim.getLineIn(Minim.STEREO);

  audioFFT = new FFT(audio.bufferSize(), audio.sampleRate());
  audioFFT.linAverages(audioRange);

  // audioFFT.window(FFT.NONE);
  audioFFT.window(FFT.GAUSS);

  // controllers
  ampCtrl = new ControlP5(this);
  ampCtrl.addSlider("ampVal")
    .setPosition(stageM + labelW + valuePadding, stageM + audioMax + (valuePadding * 2) + rectS)
    .setSize((audioRange * rectS) - labelW - valuePadding, rectS)
    .setRange(0.0f, 600.0f)
    .setValue(25.0f)
    .setColorCaptionLabel(color(20));

  indexCtrl = new ControlP5(this);
  indexCtrl.addSlider("indexVal")
    .setPosition(stageM + labelW + valuePadding, stageM + audioMax + (valuePadding * 3) + (rectS * 2))
    .setSize((audioRange * rectS) - labelW - valuePadding, rectS)
    .setRange(0, 1000)
    .setValue(500)
    .setColorCaptionLabel(color(20));
  
  stepCtrl = new ControlP5(this);
  stepCtrl.addSlider("stepVal")
    .setPosition(stageM + labelW + valuePadding, stageM + audioMax + (valuePadding * 4) + (rectS * 3))
    .setSize((audioRange * rectS) - labelW - valuePadding, rectS)
    .setRange(0, 1000)
    .setValue(250)
    .setColorCaptionLabel(color(20));
  
  // rangeCtrl = new ControlP5(this);
  // rangeCtrl.addSlider("rangeVal")
  //   .setPosition(stageM + labelW + valuePadding, stageM + audioMax + (valuePadding * 5) + (rectS * 4))
  //   .setSize((audioRange * rectS) - labelW - valuePadding, rectS)
  //   .setRange(1, 256)
  //   .setValue(12)
  //   .setColorCaptionLabel(color(20));


} 

// ================================================================
public void draw() {
  background(bgC);

  // linking controllers
  audioAmp = ampVal;
  audioIndex = (float)indexVal / 1000;
  audioIndexStep = (float)stepVal / 1000;
  // audioRange = rangeVal;

  // update display
  settingsStr = "amplifier: " + audioAmp + " // index start: " + audioIndex +  " // index step: " + audioIndexStep;

  audioFFT.forward(audio.mix);

  // Render stage
  fill(25); noStroke(); 
  rect(stageM, stageM - (valuePadding / 2), audioRange * rectS, audioMax + valuePadding);

  // Print values
  fill(50); noStroke();
  rect(stageM, stageM - (rectS + (valuePadding * 2)),(audioRange * rectS), rectS);

  fill(0xff00AE55);
  textAlign(LEFT);
  text(settingsStr, stageM + (rectS / 4), stageM - (rectS + (valuePadding * 2)) + (rectS  - (rectS / 3)));



  for (int i = 0; i < audioRange; ++i) {

    float indexAvg = (audioFFT.getAvg(i) * audioAmp) * audioIndexAmp;
    float indexCon = constrain(indexAvg, 0, audioMax);

    // Render bars
    noStroke();
    if (indexCon < 35) fill(0xff00AE55, 255);
    else if (indexCon < 75) fill(0xffDD6600, 255);
    else fill(0xffFF6644, 255);
    
    rect(xStart + (i * xSpace) + (valuePadding / 2), yStart + audioMax, rectS - valuePadding, - indexCon );
    // rect(xStart + (i * xSpace) + (valuePadding / 2), yStart, rectS - valuePadding, indexAvg );

    // Print values
    fill(50); noStroke();
    rect(xStart + (i * xSpace) + (valuePadding / 2), stageM + audioMax + valuePadding + (valuePadding / 2), rectS - valuePadding, rectS - valuePadding );

    fill(0xff00AEFF);
    textAlign(CENTER);
    text(str((int)indexAvg), xStart + (i * xSpace) + (rectS / 2), stageM + audioMax + valuePadding + (rectS  - (rectS / 3)));

    audioIndexAmp += audioIndexStep;      
  }

  audioIndexAmp = audioIndex;

  // controller labels
  fill(50); noStroke();
  rect(stageM, stageM + audioMax + (valuePadding * 2) + rectS, labelW, rectS);
  fill(0xffffa99a); textAlign(LEFT);
  text("amplifier", stageM + (rectS / 4), stageM + audioMax + (valuePadding * 2) + rectS + (rectS  - (rectS / 3)));
  
  fill(50); noStroke();
  rect(stageM, stageM + audioMax + (valuePadding * 3) + (rectS * 2), labelW, rectS);
  fill(0xffffa99a); textAlign(LEFT);
  text("index", stageM + (rectS / 4), stageM + audioMax + (valuePadding * 3) + (rectS * 2) + (rectS  - (rectS / 3)));
  
  fill(50); noStroke();
  rect(stageM, stageM + audioMax + (valuePadding * 4) + (rectS * 3), labelW, rectS);
  fill(0xffffa99a); textAlign(LEFT);
  text("step", stageM + (rectS / 4), stageM + audioMax + (valuePadding * 4) + (rectS * 3) + (rectS  - (rectS / 3)));

  // fill(50); noStroke();
  // rect(stageM, stageM + audioMax + (valuePadding * 5) + (rectS * 4), labelW, rectS);
  // fill(#ffa99a); textAlign(LEFT);
  // text("range", stageM + (rectS / 4), stageM + audioMax + (valuePadding * 5) + (rectS * 4) + (rectS  - (rectS / 3)));

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
