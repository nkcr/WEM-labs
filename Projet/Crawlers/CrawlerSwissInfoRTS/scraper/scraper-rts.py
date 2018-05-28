import scrapy
import datetime
import dateutil.parser
import hashlib
import logging
import locale
locale.setlocale(locale.LC_TIME, "fr_CH")

from pymongo import MongoClient
client = MongoClient('localhost', 27017)

db = client['rts']
swissinfo_collection = db['rts-04-collection']

logging.getLogger('scrapy').setLevel(logging.WARNING)

class BlogSpider(scrapy.Spider):
  count = 0
  name = 'blogspider'
  start_urls = ['https://www.rts.ch/info/economie/9482575-apres-le-scandale-du-diesel-herbert-diess-prend-les-renes-de-volkswagen.html']

  def parse(self, response):
    article = self.build_article(response)
    post_id = swissinfo_collection.insert_one(article).inserted_id
    self.count = self.count + 1
    print(self.count, ">>>>>>>>>>> inserted id: ", post_id)

    for related_article in response.css('div.rts-module-related-elements article.content-item > a'):
      link = related_article.xpath('@href').extract_first()
      print(">>>>>> link: ", link)
      if not link.endswith("pdf") and link.startswith("/info"): 
        yield response.follow(related_article, self.parse)

  def build_article(self, response):
    article = {}

    title = response.css('div.rts-module-title > h3').xpath('text()').extract_first()
    source = "rts"
    articleDate = self.getDate(response)

    
    article['id'] = hashlib.md5("{}{}{}".format(source, articleDate, title).encode('utf-8')).hexdigest()
    article['title'] = title
    article['url'] = response.url
    article['html'] = response.css('div.main > article').extract_first()
    article['tags'] = response.css('div.rts-module-homesection-name a').xpath('text()').extract()
    article['source'] = source
    article['crawlDate'] = int(datetime.datetime.now().timestamp())
    article['articleDate'] = articleDate

    return article

  def getDate(self, response):
    date = response.css('div.rts-module-infosport-headline.end div.inner-module > p.date').xpath('span[1]/text()').extract_first().strip()
    if date is None:
      date = "Publié le 01 janvier 1970"
    d = datetime.datetime.strptime( date, "Publié le %d %B %Y" )

    return int(d.timestamp())