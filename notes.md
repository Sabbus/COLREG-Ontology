# Domande per Daniele

1. Ci sono dei problemi con alcune object property che sarebbero per loro natura ternarie ma che io ho trattate come binarie; esempio: la proprietà *hasLightsInSight* è naturalmente ternaria perché metterebbe in relazione due *Vessel* e un *LightConfiguration* nel senso che dal *Vessel* "a", è possibile vedere la *LightConfiguration* "b" che appartiene al *Vessel* "c". Io questa relazione l'ho trattata come binaria ipotizzando che il *Vessel* "a" sia una *OwnShip*, di conseguenza il *Vessel* "c" è univocamente identificabile come il *TargetShip* partecipante alla stessa *Situation* del *Vessel* "a". Questa cosa è corretta?

2. Per ora la classe *Light* è descritta come un concetto nominale dove gli elementi enumerati sono tutte le luci possibili descritte dalla COLREG. Alcune configurazioni di luci presentano doppioni di alcune luci (due *masthead_light*, due *red_all-round_light*, ecc.); per ora questa cosa l'ho rappresentata creando instanze della classe *Light* che rappresentano questi doppioni, la cosa però è poco elegante e per questo avevo pensato di convertire le luci da individui e classi così da poter usare restrizioni cardinali. Il problema è che non è possibile scrivere assiomi che coinvolgono restrizioni cardinali di inversi di proprietà transitive quale *constantProperPartOf*. Cosa possiamo fare? Manteniamo il mio tapullo o cambiamo qualcosa?

3. Egocentricamente a suo tempo avevo messo il mio nome nel'IRI di base dell'ontologia, per la pubblicazione penso sarebbe meglio cambiarlo; cosa possiamo metterci?

4. La classe *VesselConstrainedByHerDraught* risulta insoddisfacibile se posta come sottoclasse di *PowerDrivenVessel* in quanto le due classi dovrebbero essere disgiunte per evitare classificazioni ambigue. Tuttavia la normativa specifica chiaramente che una *vesselConstrainedByHerDraught* è una *PowerDivenVessel*, come possiamo fare? Possiamo ignorare la normativa giustificando con necessità di applicazione?

5. Ho aggiunto configurazioni di luci e segnali diurni (*Shape*). Nessuna di queste classi o loro sottoclassi hanno degli assiomi di equivalenza perché ho trovato molti problemi di insoddisfacibilità e inconsistenza. La soluzione che ho trovato è usare sono assiomi di superclasse, che sembrano funzionare bene, ma sembrano un po' incasinati.

