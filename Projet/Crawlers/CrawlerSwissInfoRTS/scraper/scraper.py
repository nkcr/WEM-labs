import scrapy
import datetime
import dateutil.parser
import hashlib
import logging

from pymongo import MongoClient
client = MongoClient('localhost', 27017)

db = client['swissinfo']
swissinfo_collection = db['swissinfo20-04-collection-multi']

logging.getLogger('scrapy').setLevel(logging.WARNING)

class BlogSpider(scrapy.Spider):
  count = 0
  name = 'blogspider'
  start_urls = ['https://www.swissinfo.ch/fre/analyse-du-vote-du-4-mars_-no-billag--n-a-pas-convaincu-les-jeunes/44061606']

  def parse(self, response):
    article = self.build_article(response)
    post_id = swissinfo_collection.insert_one(article).inserted_id
    self.count = self.count + 1
    # print(self.count, ">>>>>>>>>>> inserted id: ", post_id)

    for related_article in response.css('aside div.teaser-content > h3 > a'):
      link = related_article.xpath('@href').extract_first()
      if not link.endswith("pdf"):# and link.startswith("https://www.swissinfo.ch/fre"): 
        yield response.follow(related_article, self.parse)

  def build_article(self, response):
    article = {}

    title = (" ".join(response.css('h1[itemprop="name headline"]').xpath('text()').extract())).strip()
    source = "swissinfo"
    articleDate = self.getDate(response)

    
    article['id'] = hashlib.md5("{}{}{}".format(source, articleDate, title).encode('utf-8')).hexdigest()
    article['title'] = title
    article['url'] = response.url
    article['html'] = response.css('div[itemprop="articleBody"]').extract_first()
    article['tags'] = response.css('div.tags-wrapper').xpath('ul/li[3]/span/a/span/text()').extract()
    article['source'] = source
    article['crawlDate'] = int(datetime.datetime.now().timestamp())
    article['articleDate'] = articleDate

    return article

  def getDate(self, response):
    date = response.css('div[itemprop="articleBody"]').xpath('meta[@itemprop="datePublished dateModified"]/@content').extract_first()
    if date is None:
      date = response.xpath('//*[@id="mainArticle"]//time/@datetime').extract_first()
    if date is None:
      date = "1970-01-01T00:00:00Z"
    return int(dateutil.parser.parse(date).timestamp())