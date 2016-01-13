Team ByteeCat
Baclava Georgiana-Liliana 322CB
Daraban Alexandra-Mihaela 322CB (capitan)
Ifrim Andreea-Carmen 322CB
Lupancescu Diana 322CB

	Pentru etapa finala, ama ales sa pastram bot-ul din etapa precedenta.
	Am folosit o strategie ce consta in implementarea mai multor metode, cum ar fi:

- getNumberOfEnemies(Region region, String opponentName) - aceasta metoda returneaza numarul de armate 
inamice aflate in regiunile vecine;

- getNumberOfFreeRegions(Region region, BotState state) - cu ajutorul acestei metode, se returneaza
 numarul de regiuni libere din vecinatatea unei anumite regiuni data ca parametru

- getNumberOfEnemyRegions(Region region, String opponentName) -aceasta metoda returneaza numarul de 
regiuni vecine detinute de inamic 

- average( int attack, int defense) - metoda prin care se calculeaza numarul trupelor care mor in atac
 si in aparare. Pentru calculul numarului de trupe care mor din cele care se afla in defensiva, se 
calculeaza 2 valori: 60% din numarul de trupe care ataca si fiecare trupa care ataca are 60% sanse sa 
omoare un inamic. Se aduna trupele omorate. Intre cele 2 valori calculate se face o medie, prima 
valoare avand ponderea de 84%, iar cea de-a doua cu 16%. Pentru a calcula nr de trupe care mor din 
cele aflate in ofensiva, se foloseste acelasi principiu, doar ca probabilitatea de 60% este inlocuita 
cu 70%.

- successfulAttack(Region region, String name) - metoda ce returneaza adevarat daca regiunea poate fi 
atacata cu succes de un anumit jucator. Aceasta metoda se foloseste de metoda average();

- getImportance(Region region, String myName, String opponentName) - metoda ce calculeaza importanta 
unei regiuni in functie de ocupatia sa si de supraRegiunea din care face parte. Daca regiunea este 
detinuta de inamic importanta teritoriului creste de 3 ori.

- sort(ArrayList<Region> regions, String myName, String opponentName) - metoda ce sorteaza in ordine 
descrescatoare lista de regiuni primita ca parametru, in functie de importanta. Aceasta metoda are ca
scop sortarea listei astfel incat regiunile detinute de oponent sa fie prioritare.

- getStartingRegion(BotState state, Long timeOut) - metoda ce alege teritoriul de inceput. Daca nu 
exista o regiune in harta vizibila, se alege o versiune random; in contrar, se alege o regiune ce se
afla in vecinatatea unei regiuni detinute de jucator.

- getPlaceArmiesMoves(BotState state, Long timeOut) - aceasta metoda este apelata la inceputul fiecarei 
runde si adauga armate pe harta. Se realizeaza o lista ce contine toate regiunile detinute de bot ce se
invecineaza cu regiuni inamice sau libere. Aceasta lista este sortata in functie de gradul de pericol 
in care se afla (cu cat numarul de armate inamice este mai mare, cu atat se afla intr-un pericol mai 
mare. Se parcurge aceasta lista si se calculeaza numarul minim de armate ce trebuie plasate in aceasta 
regiune astfel incat regiunea sa devina "safe". Daca acest lucru nu este posibil, se trece la 
urmatoarea regiune pentru ca minimiza numarul de armate ce ar putea fi pierdute. Daca, dupa aceasta 
parcurgere, mai exista armate ce trebuie plasate, se parcurge iar lista, plasandu-se cate o armata pe 
fiecare din regiunile din lista pana cand nu mai exista nici o armata de plasat.

- getAttackTransferMoves(BotState state, Long timeOut) -  aceasta metoda reprezinta etapa a doua a 
fiecarei runde. Se realizeaza o lista cu fiecare regiune libera sau detinuta de inamic din vecinatatea 
regiunilor noastre ce ar putea fi atacata cu succes si se sorteaza in functie de importanta ei. 
Se parcurge lista de atacuri posibile si se verifica daca e detinuta de adversar sau e libera. Daca
este detinuta de inamic, se ataca cu 150% din numarul de armate ramase in acea regiune. Acest lucru
are loc pentru fiecare regiune in care atacul e posibil pana cand numarul de armate ramase in acea 
regiune devine 0. Daca este o regiune libera, in functie de apartenenta sa la lista de wasteland-uri, 
se ataca cu 10 sau 4 armate.
Pentru transfer, parcurgem lista de regiuni detinute de jucator iar pentru fiecare regiune in parte se 
realizeaza o lista cu regiunile vecine detinute de jucator. Pentru fiecare regiune vecina, verificam 
daca are in jurul ei regiuni inamice si in functie de aceasta facem transferul. 
In continuare, daca regiunea curenta este inconjurata doar de regiuni ce nu se afla in proximitatea
regiunilor inamice, putem muta armatele catre prima regiune-vecina ce se afla langa o regiune libera.
Astfel, regiunile cu cele mai putine armate se vor afla in interior, iar cele cu cele mai multe armate
spre exterior.


	Jocul se va desfasura incepand cu alegerea regiunilor, cu ajutorul metodei getStartingRegion.
Daca avem prima mutare (nu avem nici o regiune aleasa), alegem o regiune random din cele valabile. 
In caz contrar, verificam prin getEnemies daca exista teritorii detinute de noi in vecinatatea vreunei
regiuni din lista de regiuni valabile. 
 	Dupa ce se va face alegerea regiunilor, va incepe prima runda, unde se vor plasa armate in 
functie de teritoriile vecine, daca in invecinarea teritoriului nostru sunt armate inamice atunci aici 
se vor plasa armate, pentru ca teritoriul nostru sa nu fie atacat, acest lucru il facem cu ajutorul 
unei liste pe care o vom sorta dupa pericolul in care se afla regiunea respectiva, daca nu se vor 
putea plasa suficiente armate incat regiunea sa devina "safe", atunci se va trece la urmatoarea 
regiune. Numarul minim de armate plasate intr-o regiune este pus astfel incat regiunea sa devina sigura.
 Daca inca vor mai ramane armate de plasat atunci ele se vor plasa pe regiunile cu vecini adversari 
sau regiuni goale. 
	In etapa a doua, etapa de atac si transfer, in primul rand verificam daca putem ataca cu 
succes o regiune inamica sau libera, folosind functia successfulAttack, apoi sortam lista de regiuni 
pe care le putem ataca cu succes in functie de importanta acestora si vom ataca numai daca teritoriile
 raman sigure si dupa atac, in caz ca se mai afla si alte regiuni inamice in apropiere. 
Vom ataca numai daca se poate ataca cu mai mult de o armata si daca mai ramane cel putin o armata in 
regiunea din care pornim atacul. Daca regiunea ce trebuie atacata este detinuta de adversar, atacam
cu 150% din numarul de armate ce se afla in acea regiune. Daca este o regiune libera, verificam
daca este un wasteland. In functie de apartenenta sa la lista de wasteland-uri din harta, atacam cu 10
(daca este wasteland) sau 4 armate (in caz contrar). Transferul se va face in momentul in care intr-o 
regiune vom avea mai mult de o armata. Daca vom fi inconjurati de teritorii aliate, transferul se va 
face la regiunile inconjurate de inamici. Daca nu avem nici un vecin ce se afla langa o regiune inamica,
facem transferul la primul teritoriu ce se afla langa o regiune libera.