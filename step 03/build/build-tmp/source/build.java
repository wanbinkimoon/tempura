import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.*; 
import ddf.minim.analysis.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class build extends PApplet {

int bgC       = 0xff2F2F2F;
String dataPATH = "../../data/";

// ================================================================

int     renderNum  = 0;
String  renderPATH = "../render/";

// ================================================================




// ================================================================

Minim minim;
AudioInput audio;
FFT audioFFT;

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
} 

// ================================================================
public void draw() {
  background(bgC);

  audioFFT.forward(audio.mix);

  for (int i = 0; i < audioRange; ++i) {

    float indexAvg = (audioFFT.getAvg(i) * audioAmp) * audioIndexAmp;
    float indexCon = constrain(indexAvg, 0, audioMax);

    // Render bars
    noStroke();
    fill(255, 35);
    
    rect(xStart + (i * xSpace) + (valuePadding / 2), yStart + audioMax, rectS - valuePadding, - indexCon );
    // rect(xStart + (i * xSpace) + (valuePadding / 2), yStart, rectS - valuePadding, indexAvg );

    // Print values
    fill(20); noStroke();
    rect(xStart + (i * xSpace) + (valuePadding / 2), stageM + audioMax + valuePadding + (valuePadding / 2), rectS - valuePadding, rectS - valuePadding );

    fill(0xff00aeFF);
    textAlign(CENTER);
    text(str((int)indexAvg), xStart + (i * xSpace) + (rectS / 2), stageM + audioMax + valuePadding + (rectS  - (rectS / 3)));

    audioIndexAmp += audioIndexStep;      
  }

  audioIndexAmp = audioIndex;
  
  noFill();
  stroke(0xffBB6600); 
  line(stageM, stageM, width - stageM, stageM);
  stroke(0xffDD6600); 
  line(stageM, stageM + 100, width - stageM, stageM + 100);

  // Print values
  fill(20); noStroke();
  rect(stageM, stageM - (rectS + valuePadding),(audioRange * rectS), rectS);

  fill(0xff00AE55);
  textAlign(LEFT);
  text(settingsStr, stageM + (rectS / 4), stageM - (rectS + valuePadding) + (rectS  - (rectS / 3)));


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