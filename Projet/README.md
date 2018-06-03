# 1. Contexte et objectifs du projet

Ce projet s'inscrit dans le cours MSE "Web Mining" du semestre de printemps 2018. L'objectif est de mettre en pratique des techniques de crawling et d'indexation sur des pages WEB afin d'en extraire des informations. Ainsi, le projet porte sur deux grosses parties: crawling du WEB et analyse des données. La partie crawling et analyse des données est libre et peut porter sur un sujet à choix suivant les éléments théoriques apportés durant le cours et d'autres ressources à choix.

L'objectif de notre projet est d'analyser des articles de journaux et ligne afin d'en extraire les thématiques importantes qui ressortent au fil du temps. Nous sommes donc en résumé intéressé à visualiser l'évolution des thématiques d'actualité en fonction du temps. Notre système devrait ainsi nous permettre de voir si certaines thématiques en engendrent d'autre ou si certaines thématiques sont récurentes au fil du temps. Nous espérons aussi pouvoir faire certaines déduction inatendues nous permettant de réfléchir et se sensibiliser par rapport aux médias du WEB.

Notre travail n'est pas révolutionnaire par rapport aux éventuelles techiques utilisées mais se différencie par rapport aux fonctionnalités que nous proposons et la manière de traiter les données ainsi que leurs sources.

# 2. Données (sources, quantité, évtl. pré-traitement, description)

## Sources

Voici les sources sélectionnées pour le crawling des données:

* [RTS info](http://www.rts.ch/info/)
* [SwissInfo](https://www.swissinfo.ch)
* [20minutes romandie](http://www.20min.ch/ro/)
* [Le temps](https://www.letemps.ch)

Pour chaque source le même concept d'extraction a été utilisé. Il s'agit d'extraire la section contenant l'article (son *body*) et de suivre les articles en lien proposé par le site. Cette méthode permet, en sélectionnant un bon article de base, d'extraire une grande quantité d'article, voir même l'entièreté des articles disponibles.

## Quantité

Voici un résumé de la quantité d'article extrait pour chaque source:

|RTS info|Swissinfo|20 minutes|Le temps|
|--------|---------|----------|--------|
|7'547   |15'483   |103'115   |7'083   |

Total d'articles: **133'228**

## Support de sauvegarde

Comme support de sauvegarde, nous utilisons *MongoDB* via le service *AliCloud*. Ce service, allié avec la librairie *pyMongo*, nous permet de facilement manipuler et sauvegarder nos données.

## Scripts d'extraction

Les différents scripts d'extractions sont disponibles dans le dossier `Crawlers/`. Deux des crawler sont fait en *Java* avec *crawler4j* et un autre (celui pour Swissinfo et RTS) est fait en *Pyhton* avec *Scrappy*. Cette différence s'explique par les choix personnel des développeurs.

## Description

Pour chaque article, la même structure a été sauvegardée. La structure de donnée typée des articles est décrite par la structure suivante:

```go
type Article struct {
  Id           bson.ObjectId `json:"id" bson:"_id,omitempty"`
  Title        string        `bson:"title"`
  Url          string        `bson:"url"`
  Html         string        `bson:"html"`
  ClearedHTML  string        `bson:"clearedHTML"`
  CleanContent string        `bson:"cleanContent"`
  Source       string        `bson:"source"`
  CrawlData    long          `bson:"crawlDate"`
  ArticleDate  long          `bson:"articleDate"`
  Tags         []string      `bson:"tag"`
  BiMonth      long          `bson:"biMonth"`
}
```
Certain champs on été introduit après le crawling dans le processus de pre-processing discuté au point suivant.

Le tableau suivant décrit le contenu des champs:

* **Id**: identificateur unique de l'article. Utilisé par notre SGDB.

* **Title**: Titre de l'article

* **Url**: URL de l'article

* **Html**: Contenu *brut* de l'article (son *body* HTML)

* **ClearedHTML**: Contenu textuel extrait du contenu *brut*. Opération effectuée en post-processing après la phase de crawling.

* **CleanContent**: Contenu textuel après étape de *cleaning* (décrite après). Opération effectuée en post-processing.

* **Source**: Source de l'article (RTS, le Temps, ...)

* **CrawlDate**: Date d'extraction du contenu sous forme de timestamp.

* **ArticleDate**: Date de parution de l'article ou sa dernière mise à jour, également sous forme de timestamp.

* **Tags**: Tags de l'article si disponible. Vu que toutes les sources n'en ont pas un, ne sera finalement pas utilisé.

* **BiMonth**: Indicateur de la position du l'article par moitier de mois de l'année sous la forme <année><numéro de demi-mois>. Ainsi, le numéro 201801 indique un article paru entre début janvier et mi-janvier 2018. 201504 indique un article paru entre mi-février et fin-février 2015.

## Traitement des données

Il a été nécessaire d'effectuer une étape de post-traitement après le crawling pour insérer les champs suivants:

* *clearedHTML*
* *CleanContent*
* *BiMonth*

### ClearedHTML

Cette étape est nécessaire pour nettoyer les balises HTML et avoir uniquement le texte des articles. Le script qui fait cette opération se situe dans `cleaner/batchProcess.go`. Le contenu est extrait en sélectionnant tout ce qui se trouve entre des balise `<p></p>`. Pour des soucis de performance l'implémentation est réalisée pour une exécution asychrone en travaillant sur des batchs de données (pour réduire les temps de transaction réseau) avec le language *go*. Ce script insère également le champs *biMonth*.

### BiMonth

Ce champs est nécessaire pour nous faciliter le regroupement des article par période de temps. Le découpage est fait en demi-mois afin de pouvoir sélectionner des périodes de temps que se chevauchant. En effet, un thème n'est pas forcément discuter du début d'un mois à la fin de celui-ci, mais peut se dérouler sur une période de temps chevanchant un mois. Nous sélectionerons donc des périodes se chavanchant (première pédiode se déroulant tout le mois de janvier, deuxième période de mi-janvier à mi-février, troisième période sur tout le mois de février, ect...).

Cette opération est aussi effectuée dans le script `cleaner/batchProcess.go`.

### CleanContent

Ce champs contient une version purifiée du contenu textuel *ClearedHTML*. Le script qui effectue l'étape de purification se trouve dans `cleaner/processLine.py`. Python a été utilisé pour ses librairies riches de traitement de text. Pour des soucis de performance, l'exécution est effectuée en *go* par `cleaner/batchProcessPP.go`. Le script *go* va faire appel au script *python*.

Voici les étapes de purification:

* Tokenization à l'aide de la méthode `word_tokenize` de la librairie `nltk`
* Remplacement de tout les caractères qui ne sont pas les suivants par des espaces: `[A-Za-z0-9()èéàÉÀêÊçÇ]`
* Stemmization en français des mots à l'aide de `snowball` (`nltk`)
* Filtrage de tous les mots qui sont des numéros
* Filtrage de tous les mots inférieurs à 3 caractères

## Génération du corpus de mots

Le notebook `cleaner/Pre_processing.ipynb` contient différentes méthodes qui nous ont permis d'extraire un corpus de mots à partir de tous nos articles. Différents corpus on été généré et sauvegardé dans une base de données. Nous avons fait plusieurs version du corpus de mots:

* *raw_corpus*: version simple avec uniquement une transformation `lower()`
* *greater_than_two*: version se basant sur *raw_corpus* mais éliminant les mot inférieurs à 3 charactères
* *only_alpha*: version se basant sur *greater_thant_two* mais supprimant les `numeric`
* *french_stemmed*, *english_stemmed* et *french_english_stemmed*: version se basant sur *only_alpha* et proposant le stemming français, anglais et français + anglais respectivement
* *special_chars*: version se basant sur *alpha_only* et supprimant tous les caractère spéciaux

Ces différentes versions nous ont permis de tester plusieurs variantes. Nous avons finalement générer une version mettant en relation chaque mot de *special_chars* avec sa version *french_stemmed* sous forme d'un hash <alpha_only>: <french_stemmed>. C'est cette structure qui sera finalement utilisée. Le stemming anglais ne faisait pas sens et la gestion des stops words se fait après au niveau de la vectorisation des documents.

# 3. Planification, répartition du travail

Pour facilité la collaboration, nous avons utilisé un service cloud pour l'hébergement des données (AliCloud avec MongoDB) et des notebook Collab.

La répartition des tâche s'est faite suivant l'intérêt et les compétence de chacun, en veillant à ce qu'une part équitable de travail sois fournie par tous les membres du groupe. Le tableau suivant résume les activités de chaque membre:

|Tâche|Qui|
|---|---|
|Extraction du site 20 minutes|Matthias|
|Extraction du site leTemps|Simon|
|Extraction des sites RTS et Swissinfo|Noémien|
|Conception des algorithmes|Tout le monde|
|Extraction du contenu HTML des articles|Simon|
|Génération des corpus de mots et cleaning|Noémien|
|Vectorization des documents|Matthias|
|Génération des *biMonth*|Simon|
|Génération des clusters|Matthias|
|Mise en place de la vue|Noémien|
|Ecriture du Readme|Tout le monde|

# 4. Fonctionnalités / cas d’utilisation

Notre application finale n'est pas interactive mais reste statique. Ainsi, nous proposons une illustration statiques des thêmes importants sur 30 jours, avec 24 périodes se chevanchants par années, rencontré dans nos articles. L'illustration suit l'ordre chronologique et permet à l'utilisateur de voir l'évolution et d'éventuelles corrélations ou reccurences dans les thêmes proposés.

L'image suivante est un extrait de notre interface:

![IHM](https://storage.googleapis.com/nkcr_personal_storage/cdn/mse/wem-ihm.png)

# 5. Techniques, algorithmes et outils utilisés (aussi en lien avec votre exposé)

Cette partie décrit les différents algorithmes utilisé afin de récupérer les thématiques importante pour une durée précise. Le notebook Jupyter utilisé pour cette partie est **TFIdf_Clustering_Analyze/TFidf_Clustering_V3.ipynb**.

## Préparation des données

Comme mentionné précédemment, plusieurs étapse de pré-processing ont été réalisées.La purification du contenu HTML `clearedHTML`, les étapes de stemming, suppression de charactères, etc. Une version a été choisie: *special_chars* (voir plus haut). Celle-ci a donc été appliquée sur la totalité des articles afin de créer un corpus qui prendra toute son importance à la fin du processus. Il est donc important de récupérer ce corpus depuis le SGBD.

Pour le moment, il nous faut tout de même récupérer l'ensemble du dataset. On remarque en se faisant que certains articles n'ont pas de corps HTML. Environ 850 sur les 130'000 articles sont donc laissé sde côté afin de simplifier le processus. Cela se fait grâce à la commande suivante:

```python
dataframe = dataframe[dataframe['clearedHTML'] != ""].reset_index()
```

A ce stade, les données sont prêtes pour les prochaines étapes.

## TFidf-vectorization

La vectorization tf-idf est extrêmement connue dans le domaine de la Recherche d'Information (RI). Elle permet de mettre en valeur l'importance d'un mot par rapport à un document dans une collection donnée. La librairie **sklearn** fournit une classe permettant de le faire très facilement sur un corpus donné: *TfidfVectorizer*.
```python
vectorizer = TfidfVectorizer()
vectors_words_corpus = vectorizer.fit_transform(corpus)
```
Ce qui rend cette classe particulièrement intéressante sont les différents paramètres que l'on peut décider. Voici ceux que nous avons décidé d'influencer afin d'obtenir des bons résultats.

* **max_df**: pourcentage maximal de documents pouvant contenir un mot, très utile dès lors que notre souhait est de récupérer les mots caractréistiques d'un certain sujet
* **min_df**: pourcentage minimal de documents pouvant contenir un mot, si le mot n'est pas souvent mentionné, il n'est probablement pas très révélateur d'un sujet tendance
* **stop_words**: liste de stopwords, aurait put être mis dans l'étape de pré processing mais il a été choisi de le faire ici.
* **tokenizer**: function de tokenize qui sera appliquée par le vectorizateur avant de calculer le tf-idf. Cette fonction est la même que celle qui a été utilisée pour créer le corpus dans la phase de pré-processing. Celle-ci fait donc plus que simplement tokenizer.
* **ngram_range**: permet de définir si le vectorizateur va calculer le tf-idf sur des bigrams, trigrams, etc.

Le paramètre *stop_words* n'a jamais bougé. La fonction de *tonkenizer* à été validée dans l'étape de pré-processing et n'a donc jamais été modifiée durant les tests. Les trois paramètres qui ont subis quelques modifications sont *max_df*,*min_df* et *ngram_range*. Après plusieurs essais, il a été validé que *ngram_range* n'améliorait pas les résultats en augmentant. Il a été décidé de garder la valeur **1**. Tout d'abord absent, la présence de *max_df* et *min_df* a considérablement amélioré les résultats. Sans cela, les mots finaux n'étaient pas représentatifs. La présence de max_df a permis d'enlever des mots trop communs à l'ensemble des documents. La présence de *min_df* a permis d'enlever les mots trop rares à l'ensemble des documents. Ces deux paramètres ont également permis de réduire drastiquement la taille du vecteur final.

Les valeurs finales des paramètres pour cette partie sont les suivantes:

| max_df |  min_df | stop_words |         tokenizer        | ngram_range |
|--------|---------|------------|--------------------------|-------------|
|   0.8  |   0.1   |   french   | pré-processing function  |      1      |

L'étape de vectorization tf-idf permet de créer un tableau à deux dimensions avec comme colonnes les mots et comme lignes les documents. La vectorization retourne également une liste afin de retrouver à partir de l'index de la colonne le mot correspondant.

## Classification non supervisée: clustering

L'étape précédente retourne donc un tableau 2D qui pourra utilisé dans cette étape. L'objectif est de retrouver **N** différents thématiques les plus abordées durant une certaine période. Étant donné que les labels sont inexistants, partir sur une approche non supervisée semble être approprié. Le clustering a été choisi car il permet de regrouper en clusters des documents. Le but sera après de traiter les clusters séparément afin de récupérer la thématique propre à chaque partition.

La librairie **sklearn** fournit une classe permettant de faire du clustering: *KMeans*. Cet algorithme de clustering nonhiérarchique, bien connu, est implémenté par la librairie. Voici sonutilisation dans le cadre de ce projet:
```python
kmeans_fitted = KMeans(n_clusters=nb_clusters)
kmeans_fitted.fit(value_matrix,)
```
KMeans fournit une quantité de paramètres permettant d'influencer le positionnement initial des clusters, le nombre d'itérations avant arrêt, etc. Concernant ce projet, seul un paramètre nous a intéressé, à savoir le nombre de clusters **n_clusters**. Ce nombre de clusters représentera le nombre de thématiques que nous souhaitons découvrir dans le corpus.

La valeur finale du paramètre **n_clusters** est le suivant:

| n_clusters |
|------------|
|      5     |

Il y aura donc 5 thématiques par corpus!

Cette étape va retourner l'objet kmeans afin d'être analysé.

## Analyse du Clustering

L'objectif de cette dernière phase consiste à analyser les différents clusters. En utilisant le résultat du clustering, le vocabularie fournit par la tf-idf vectorization ainsi que le corpus entier généré dans la phasedepré-processing, tout les éléments sont réunis afin de récupérer les sujets importants.

L'idée consiste à analyser le centroide de chaque cluster. On trie ce vecteur par ordre décroissant et on récupérant les indices. Puis on garde uniquement un certain nombre de mots via un paramètre externe **nb_words_for_cluster**. Le paramètre

La valeur finale du paramètre **nb_words_for_cluster** est le suivant:

| nb_words_for_clusters |
|-----------------------|
|           6           |

Cette phase retourne une liste contenant **n** listes de mots (n => nombre de cluster). Chaque liste de mots contiendra **m** mots (m => nb_words_for_cluster).

## Mise en forme et upload vers MongoDB

Les différentes étapes mentionnées dans ce chapitres doivent être réunies afin de produire le résultats final. Tout d'abord, il faut définir les constantes pour les paramètres. Puis, il faut produire les tuples *biMonth* afin de parcourir uniquement les articles dans une certaine période de temps.
```python
tuples_biMonth = [(unique_biMonth[i], unique_biMonth[i+1]) for i in range(len(unique_biMonth)-1)]
```
Puis on peut appliquer le processus pour chaque tuple *biMonth*: tf-idf vectorization -> clustering -> analyse du clustering. Ceci est décrit dans la méthode **topicAnalysisProcessingForMonth**. La méthode **topicAnalysisProcessing** va appliquer la méthode précédente sur chaque tuple *biMonth*.

Finalement, les résultats sont stockés sous la forme d'un dictionnaire dans MongoDB.

## Visualisation

Pour la visualisation, la librairie *Graphviz* a été utilisée. Toutes les étapes pour générer la visualization sont disponible dans le notebook `visualization/Visualization.ipynb`.

La visualisation travail avec une structure de données stockant pour chaque période les différents cluster avec leurs mots. La structure, sauvegardée une seule fois dans la DB pour ne pas devoir la générer à chaque fois, à la structure suivante:

```python
data = {
    "201801-201802": [
        ["cheval", "chien", "fourmis"],
        ["bateau", "avion", "train"]
    ],
    "201802-201803": [
        ["clavier", "souris", "ram"],
        ["france", "suisse", "italie"]
    ],
    "201804-201805": [
        ["lettre", "chiffre", "alphabet"],
        ["table", "chaise", "fauteuil"]
    ],
    "201803-201804": [
        ["cable", "adaptateur", "ipod"],
        ["rail", "cola", "tabac"]
    ]
}
```

Sous forme d'un dictionnaire, chaque clé enregistre la période de temps sous forme d'un string de *biMonth* puis comme valeur un liste pour chaque cluster contenant elle-même une liste des mots importants. La visualization va trier le dictionnaire par rapport aux clé puis parcourir les cluster et leurs mots.

# 6. Conclusion
Ce projet a permis d'entraîner et de mettre en pratique plusieurs techniques dans un même panier. Du crawling initial à la visualisation finale en passant par des étapes de pré-processing et de clustering, ce projet fut varié et intéressant. Le crawling a pris du temps, car nous avons trouvés cela intéressant de tester des crawlers différents. De plus, chaque site web source étant différent, trois développeurs n'étaient pas de trop pour implémenter les différents parseurs. Cela nous a permis de collecter une importante quantité d'articles. Pour notre objectif, 130'000 articles étaient plus que bienvenus. 

Puis s'ensuivirent, en parallèle, des étapes de pré-processing et de clustering. Il a fallu nettoyer les articles et générer les différentes étapes de pré-processing. Ces étapes très importantes ont permis d'obtenir des meilleurs résultats. En parallèle, le clustering avançait et commençait à générer des thématiques intéressantes. Évidemment, certains choix se sont avérés mauvais et nous ont parfois forcés à revenir en arrière. Finalement, c'est en mettant le tout ensemble que nous pouvons déclarer aujourd'hui que nous sommes contents du résultat. Des améliorations pourraient être les bienvenus. Par exemple, il serait envisageable d'essayer certaines techniques pour garder les noms de ville ou les noms-prénoms ensemble. Un exemple serait que *New York* soit maintenu ensembles car il est quasiment impensable que ces deux mots soient dissociés l'un de l'autre. *Washington* ne souffre pas de ce défaut. De plus, bien qu'ayant été adaptés au fur et à mesure du projet, certains paramètres pourraient être testés davantage. Faute de temps, il a fallu se contenter de certaines valeurs de paramètres. Cependant, les résultats sont déjà très satisfaisants.

