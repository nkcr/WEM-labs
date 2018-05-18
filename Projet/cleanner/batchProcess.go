package main

import (
	"fmt"
	"github.com/PuerkitoBio/goquery"
	"gopkg.in/mgo.v2"
	"gopkg.in/mgo.v2/bson"
	"strings"
	"sync"
	"time"
	"os"
)

const DB = "news"
const COL = "rts"
const size = 500
const start = 0

type Article struct {
	Id          bson.ObjectId `json:"id" bson:"_id,omitempty"`
	Title       string        `bson:"title"`
	Url         string        `bson:"url"`
	Html        string        `bson:"html"`
	ClearedHTML string        `bson:"clearedHTML"`
	Source      string        `bson:"source"`
	CrawlData   uint          `bson:"crawlData"`
	ArticleDate int64         `bson:"articleDate"`
	Tags        []string      `bson:"tag"`
}

func main() {
	session, err := mgo.Dial(os.Getenv("MGO"))
	defer session.Close()
	if err != nil {
		panic(err)
	}
	n, err := session.DB(DB).C(COL).Count()
	if err != nil {
		panic(err)
	}
	fmt.Printf("Number of items in the collection: %d\n", n)
	var wg sync.WaitGroup
	for i := start; i < n; i += size {
		wg.Add(1)
		go bb(session, i, size, &wg)
	}
	wg.Wait()
}

func bb(session *mgo.Session, i int, size int, wg *sync.WaitGroup) {
	defer func() {
		wg.Done()
		fmt.Printf("Worker %d done !\n", i/size)
	}()
	var articles = batch(session, i, size)
	fmt.Printf("Worker %d is processing %d articles (page starts at idx %d)\n", i/size, len(articles), i)
	var bulk = session.New().DB(DB).C(COL).Bulk()
	bulk.Unordered()
	for a := 0; a < len(articles); a++ {
		process(articles[a], bulk)
		processDate(articles[a], bulk)
	}
	bulk.Run()
}

func process(article *Article, bulk *mgo.Bulk) {
	p := strings.NewReader(article.Html)
	doc, _ := goquery.NewDocumentFromReader(p)
	var text string
	doc.Find("p").Each(func(i int, el *goquery.Selection) {
		text += strings.TrimSpace(el.Text())
	})
	article.ClearedHTML = text
	bulk.Update(bson.M{"_id": article.Id}, bson.M{"$set": bson.M{"clearedHTML": article.ClearedHTML}})
}

func processDate(article *Article, bulk *mgo.Bulk) {
	var date = time.Unix(article.ArticleDate, 0)
	var month = int(date.Month())
	var day = int(date.Day())
	var biMonth = 2*month - 1
	if day > 15 {
		biMonth += 1
	}
	bulk.Update(bson.M{"_id": article.Id}, bson.M{"$set": bson.M{"biMonth": (int(date.Year()) * 1000) + biMonth}})
}

func batch(session *mgo.Session, start int, size int) []*Article {
	var articles []*Article
	session.DB(DB).C(COL).Find(bson.M{}).Skip(start).Limit(size).All(&articles)
	return articles
}

