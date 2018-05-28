```
 docker run --name somemongo -p27017:27017 -v /Users/nkcr/Documents/HES-SO\ MSE/2017-2018/WEM/projet/mongo\ db/data:/data/db -d mongo
 ```

 ```
 mongodump --db swissinfo --collection swissinfo20-04-collection-multi --archive=/data/db/dumps/swisssinfo.gz
  mongodump --db swissinfo --collection swissinfo20-04-collection --archive=/data/db/dumps/swisssinfoFR.gz
 mongorestore --uri mongodb://root:WebM2018Mse@dds-4xo14776d7c78aa41621-pub.mongodb.germany.rds.aliyuncs.com:3717 --archive=data/db/dumps/swisssinfoFR.gz 
 ```

Dans la console mongo depuis robo3t

Rename collection
```
use admin
db.runCommand({renameCollection:"rts.rts-04-collection",to:"news.rts"})
```
# 15493
db.getCollection('swissinfo20-04-collection-multi').find({url: { $regex : /^.*swissinfo\.ch\/fre.*/ }}).count()


db.getCollection('allArticles').find({ clearedHTML: { $exists: true} }).count()