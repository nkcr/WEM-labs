Vous ajouterez sur votre dépôt un README comportant les rubriques suivantes:

1. Contexte et objectifs du projet

2. Données (sources, quantité, évtl. pré-traitement, description)

3. Planification, répartition du travail

4. Fonctionnalités / cas d’utilisation

5. Techniques, algorithmes et outils utilisés (aussi en lien avec votre exposé)

6. Conclusion

Ce document est à compléter au fur et à mesure de l’avancement de votre projet, il vous servira de rapport une fois finalisé. Il ne doit pas être trop long, comme ordre de grandeur on imaginera un maximum de 5-6 pages imprimées. La rubrique planification doit être complétée pour la séance du vendredi 27 avril 2018.

Si vous utilisez des algorithmes, des librairies ou du code tiers il faudra le préciser et citer vos sources. Vous préciserez aussi en quoi votre démarche et votre travail se différencient des approches existantes.

# 1. Contexte et objectifs du projet

Ce projet s'inscrit dans le cours MSE "Web Mining" du semestre de printemps 2018. L'objectif est de mettre en pratique des techniques de crawling et d'indexation sur des pages WEB afin d'en extraire des informations. Ainsi, le projet porte sur deux grosses parties: crawling du WEB et analyse des données. La partie crawling et analyse des données est libre et peut porter sur un sujet à choix suivant les éléments théoriques apportés durant le cours et d'autres ressources à choix.

L'objectif de notre projet est d'analyser des articles de journaux et ligne afin d'en extraire les thématiques importantes qui ressortent au fil du temps. Nous sommes donc en résumé intéressé à visualiser l'évolution des thématiques d'actualité en fonction du temps. Notre système devrait ainsi nous permettre de voir si certaines thématiques en engendrent d'autre ou si certaines thématiques sont récurentes au fil du temps. Nous espérons aussi pouvoir faire certaines déduction inatendues nous permettant de réfléchir et se sensibiliser par rapport aux médias du WEB. 

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

# 3. Planification, répartition du travail

# 4. Fonctionnalités / cas d’utilisation

# 5. Techniques, algorithmes et outils utilisés (aussi en lien avec votre exposé)

# 6. Conclusion
