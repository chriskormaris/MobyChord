# MobyChord

## Κατανεμημένα Συστήματα Project 
## Chord P2P σύστημα με Android κινητές συσκευές

### Πολύβιος Λιόσης, Κορμαρής Χρήστος, Μποτονάκης Δημήτριος


**Video Demos**

[MobyChord Demonstration](https://www.youtube.com/watch?v=JRSBxeAg6Mo&feature=youtu.be)

[MobyChord Simulation (Nodes: 6, 7, 0)](https://www.youtube.com/watch?v=6_RVweUqYbw)

[MobyChord Simulation (Nodes; 4, 1, 7)](https://www.youtube.com/watch?v=pip0wTNiXAQ)

[MobyChord Simulation (Nodes: 0, 1, 2, 3)](https://www.youtube.com/watch?v=qBED2lIQkvU)


Ο κόμβος που κάνει request ζητάει από έναν προκαθορισμένο κόμβο του Chord που επιλέγει, το μονοπάτι (route).
Το id του route είναι το srcPostalCode και το dstPostalCode μαζί hashαρισμένα.
Τα postal code ο client τα παίρνει μέσω της getPostalCodeFromLocation,
ενώ μέσω της getLatLngFromPostalCode παίρνει τις αντίστοιχες συντεταγμένες.
Η getLatLngFromPostalCode παίρνει σαν όρισμα ένα postal code.
Όταν βρει όλες τις παραμέτρους ο client, τις περιλαμβάνει στο passInfo και στέλνει το request.
Ο κόμβος που διάλεξε ο client εκτελεί lookUp η οποία επαναλαμβάνεται στον εκάστοτε closest preceding finger κόμβο,
έως ότου βρεθεί ο κόμβος με το key που αντιστοιχεί στο μονοπάτι. Κατόπιν, γίνεται έλεγχος αν υπάρχει το αρχείο στον δίσκο.
Αν δεν υπάρχει, ο κόμβος στέλνει request στη Google για να λάβει το json μονοπάτι και να το αποθηκεύσει σε αρχείο. 
To αρχείο έχει όνομα της εξής μορφής: hashId + "_" + srcPostalCode + "_" + dstPostalCode + ".json".
Το String που περνάει στην συνάρτηση κατακερματισμού, είναι το εξής: **srcPostalCode + "_" + dstPostalCode**.
Η συνάρτηση κατακερματισμού "hash" θα μας δώσει το id του κόμβου στον οποίο πρέπει να είναι αποθηκευμένο.
Στη συνέχεια το "hashId" του αρχείου προστίθεται ως πρόθεμα στο όνομα του αρχείου.
Παράδειγμα ονομασίας json αρχείου με πληροφορίες για ένα Route: "3_15771_11362.json"
Τέλος, τα περιεχόμενα του json αρχείου στέλνονται πίσω στον client για να ζωγραφιστεί η διαδρομή στο χάρτη.

**ΣΧΟΛΙΟ:** Η ΕΡΓΑΣΙΑ ΔΟΥΛΕΨΕ ΣΕ ΠΡΑΓΜΑΤΙΚΑ ΚΙΝΗΤΑ.


### Συνθήκες για το αν ένας κόμβος είναι closest preceding finger ενός άλλου
Για κάθε finger[i] του finger table (ξεκινώντας από το τέλος) έχουμε να συγκρίνουμε 3 μεταβλητές: finger[i].id, key, this_node.id. 
Συνολικά υπάρχουν n! μεταθέσεις, εδώ n=3, άρα n!=6.
Όλες οι πιθανές μεταθέσεις των 3 μεταβλητών είναι οι εξής:

- (finger[i].id < key < this_node.id)
- (finger[i].id < this_node.id < key)
- (key < finger[i].id < this_node.id)
- (key < this_node.id < finger[i])
- (this_node.id < finger[i].id < key)
- (this_node.id < key < finger[i])

Για να καθορίσουμε ποιες από τις παραπάνω συνθήκες ικανοποιούν το closest preceding finger ας πάρουμε το παρακάτω παράδειγμα:
η αριστερή μεταβλητή: 0, η μεσαία μεταβλητή: 1, η δεξιά μεταβλητή: 7
Εφαρμόζουμε το παράδειγμα σε κάθε συνθήκη. Τελικά οι επιθυμητές συνθήκες είναι οι εξής:

1) (finger[i].id < key < this_node.id)
finger[i].id=0, key=1, this_node.id=7:
Ο κόμβος 7 ψάχνει το key 1. Το finger 0 είναι μετά τον κόμβο 7 άρα πρέπει να προωθηθεί η αναζήτηση.

2) (key < this_node.id < finger[i])
key=0, this_node.id=1, finger[i].id=7:
Ο κόμβος 1 ψάχνει το key 0. Το finger 7 είναι μετά τον κόμβο 1 άρα πρέπει να προωθηθεί η αναζήτηση.

3) (this_node.id < finger[i].id < key)
this_node.id=0, finger[i].id=1, key=7
 Ο κόμβος 0 ψάχνει το key 7. Το finger 1 είναι μετά τον κόμβο 0 άρα πρέπει να προωθηθεί η αναζήτηση.

Αν καμία από τις παραπάνω συνθήκες δεν ικανοποιείται για κάποιο finger[i],
ο τρέχον κόμβος θα πρέπει να προωθήσει την αναζήτηση στον successor του, ο οποίος είναι πάντα το finger[0].


Συνθήκες για το αν πρέπει να στείλει ένας κόμβος κάποιο κλειδί του στον predecessor του, ο οποίος μόλις έχει συνδεθεί στο Chord ring.
Για κάθε κλειδί του κόμβου έχουμε να συγκρίνουμε 3 μεταβλητές:
pred_node.id, key, this_node.id. 
Συνολικά υπάρχουν n! μεταθέσεις, εδώ n=3, άρα n!=6.
Όλες οι πιθανές μεταθέσεις των 3 μεταβλητών είναι οι εξής:

- (pred_node.id < key < this_node.id)
- (pred_node.id < this_node.id < key)
- (key < pred_node.id < this_node.id)
- (key < this_node.id < pred_node.id)
- (this_node.id < pred_node.id < key)
- (this_node.id < key < pred_node.id)

Για να καθορίσουμε ποιες από τις παραπάνω συνθήκες χρειαζόμαστε, ας πάρουμε το παρακάτω παράδειγμα:
η αριστερή μεταβλητή: 0, η μεσαία μεταβλητή: 1, η δεξιά μεταβλητή: 7
Εφαρμόζουμε το παράδειγμα σε κάθε συνθήκη. Τελικά οι επιθυμητές συνθήκες είναι οι εξής:

1) (pred_node.id < this_node.id < key)
pred_node.id=0, this_node.id=1, key=7:
Ο κόμβος 1 έχει το κλειδί 7, άρα succ(7)=1. Όταν συνδεθεί ο κόμβος 0, τότε succ(7)=0.
Άρα το κλειδί 7 πρέπει να σταλεί στον κόμβο 0.

2) (key < pred_node.id < this_node.id)
key=0, pred_node.id=1, this_node.id=7:
Ο κόμβος 7 έχει το κλειδί 0, άρα succ(0)=7. Όταν συνδεθεί ο κόμβος 1, τότε succ(0)=1.
Άρα το κλειδί 0 πρέπει να σταλεί στον κόμβο 1.

3) (this_node.id < key < pred_node.id)
this_node.id=0, key=1, pred_node.id=7:
Ο κόμβος 0 έχει το κλειδί 1, άρα succ(1)=0. Όταν συνδεθεί ο κόμβος 7, τότε succ(1)=7.
Άρα το κλειδί 1 πρέπει να σταλεί στον κόμβο 7.

Επίσης υπάρχει ακόμα μία περίπτωση στην οποία πρέπει να σταλεί το κλειδί:
αν το κλειδί είναι ίσο με το id του predecessor -> (key == pred_node.id)


### Screenshots

**IPs**
![IPs](/Screenshots/0_ips.png)

**Keys**
![keys](/Screenshots/1_keys.png)

**Finger Tables**

Η τιμή j=0 είναι η σωστή τιμή της τρέχουσας καταχώρισης του finger table, η οποία αντιστοιχεί στην περίπτωση που όλοι οι κόμβοι του Chord δακτύλιου είναι συνδεδεμένοι.
Η τιμή j=1 είναι η πραγματική τιμή της τρέχουσας καταχώρισης του finger table, η οποία δείχνει μόνο σε κόμβους του Chord δακτύλιου που είναι ήδη συνδεδεμένοι.
![finger_table](/Screenshots/2_fingers.png)

**Crucial Data**
![crucial_data](/Screenshots/3_crucial_data.png)

**Route Files**
![files](/Screenshots/4_files.png)

**Memcache**
![cache](/Screenshots/5_cache.png)

**Request Client**

![request](/Screenshots/request.png)

**Route Map**

![map](/Screenshots/map.png)
