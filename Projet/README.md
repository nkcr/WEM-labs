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

|Id|Title|Url|Html|ClearedHTML|CleanContent|Source|CrawlDate|ArticleDate|Tags|BiMonth|
|---|---|---|---|---|---|---|---|---|---|---|
|identificateur unique|Titre de l'article|URL de l'article|Contenu *brut* de l'article (son *body* HTML|Contenu textuel extrait du contenu *brut*|Contenu textuel après étape de *cleaning* (décrite après)|Source de l'article (RTS, le Temps, ...)|Date d'extraction du contenu sous forme de timestamp|Date de parution de l'article ou sa dernière mise à jour|Tags de l'article si disponible|Indicateur de la position du l'article par moitier de mois de l'année|


# 3. Planification, répartition du travail

# 4. Fonctionnalités / cas d’utilisation

# 5. Techniques, algorithmes et outils utilisés (aussi en lien avec votre exposé)

# 6. Conclusion
