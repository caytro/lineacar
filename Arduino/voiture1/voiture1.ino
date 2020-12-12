
#include <SoftwareSerial.h>
#define ACCELERE 49 // '1'
#define RALENTI  50
#define STOP     51
#define AVANT    52
#define ARRIERE  53
#define GAUCHE   54
#define DROITE   55
#define VITESSE_MAX 100
#define DIRECT_MAX 10

// VITESSE ET SENSMOTEUR
#define PWMG_PIN     5 
#define PWMD_PIN     6 
#define DIR1G_PIN    8  
#define DIR2G_PIN    9
#define DIR1D_PIN    2  
#define DIR2D_PIN    3
#define LEDPIN       13
SoftwareSerial SerialVoiture(10, 11); // RX, TX
int BluetoothData;
int vitesse=0;
int sens=1;
int direct=0;
void setup() {
  SerialVoiture.begin(9600);
  pinMode(LEDPIN, OUTPUT);
  pinMode(PWMG_PIN, OUTPUT);
  pinMode(PWMD_PIN, OUTPUT);
  pinMode(DIR1G_PIN, OUTPUT);
  pinMode(DIR2G_PIN, OUTPUT);
  pinMode(DIR1D_PIN, OUTPUT);
  pinMode(DIR2D_PIN, OUTPUT);
  digitalWrite(LEDPIN, HIGH);
  SerialVoiture.println("Voiture prête !");
  SerialVoiture.print("Vitesse : ");
  SerialVoiture.println(vitesse);

}

void majVitessesMoteurs()
{
  int sensMoteurGauche;
  int sensMoteurDroit;
  int vitesseMoteurGauche;
  int vitesseMoteurDroit;

 
  vitesseMoteurGauche=int(sens*(vitesse+direct)*255/VITESSE_MAX);
  vitesseMoteurDroit=int(sens*(vitesse-direct)*255/VITESSE_MAX);
  sensMoteurGauche=((vitesseMoteurGauche>=0)?1:-1);
  sensMoteurDroit=((vitesseMoteurDroit>=0)?1:-1);
  analogWrite(PWMG_PIN,vitesseMoteurGauche*sensMoteurGauche);
  analogWrite(PWMD_PIN,vitesseMoteurDroit*sensMoteurDroit);
  digitalWrite(DIR1G_PIN,((sensMoteurGauche==1) ? HIGH : LOW));
  digitalWrite(DIR2G_PIN,((sensMoteurGauche==1) ? LOW : HIGH));
  digitalWrite(DIR1D_PIN,((sensMoteurDroit==1) ? HIGH : LOW));
  digitalWrite(DIR2D_PIN,((sensMoteurDroit==1) ? LOW : HIGH));
  
  SerialVoiture.println("Moteur gauche -> pin " + String(PWMG_PIN) + " : " + String(vitesseMoteurGauche*sensMoteurGauche));
  SerialVoiture.println("Moteur droit -> pin " + String(PWMD_PIN) + " : " + String(vitesseMoteurDroit*sensMoteurDroit));
}

void loop() {
  if (SerialVoiture.available()) {
    BluetoothData = SerialVoiture.read();
    SerialVoiture.print("Message reçu : ");
    SerialVoiture.println(BluetoothData);
    if (BluetoothData == ACCELERE) {
      SerialVoiture.println("Accélération !");
      vitesse+=10;
      vitesse=((vitesse>=VITESSE_MAX)?VITESSE_MAX:vitesse);
    }
    if (BluetoothData == RALENTI) {
      SerialVoiture.println("Décélération !");
      vitesse=((--vitesse >= 0) ?vitesse:0);
    }
    if (BluetoothData == STOP) {
      SerialVoiture.println("Stop !");
      vitesse=0;
    }
    if (BluetoothData == AVANT) {
      SerialVoiture.println("Marche avant !");
      sens=1;
    }
    if (BluetoothData == ARRIERE) {
      SerialVoiture.println("Marche arrière !");
      sens=-1;
    }
    if (BluetoothData == GAUCHE) {
      SerialVoiture.println("Gauche !");
      direct=((--direct<=-DIRECT_MAX)?-DIRECT_MAX:direct);
    }
    if (BluetoothData == DROITE) {
      SerialVoiture.println("Droite !");
      direct=((++direct>=DIRECT_MAX)?DIRECT_MAX:direct);
    }
    
    SerialVoiture.print("Vitesse : ");
    SerialVoiture.println(vitesse);
    SerialVoiture.print("Sens : ");
    SerialVoiture.println(((sens==1)?"Marche avant":"Marche arrière"));
    SerialVoiture.print("Direction : ");
    SerialVoiture.println(direct);
    majVitessesMoteurs();
 
   }
    digitalWrite(LEDPIN, LOW);
    delay(500);
    digitalWrite(LEDPIN, HIGH);
    delay(300);
 
}
