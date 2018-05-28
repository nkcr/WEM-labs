package main

import (
	"bytes"
	"fmt"
	"log"
	"os"
	"os/exec"
	"strings"
	"sync"

	"gopkg.in/mgo.v2"
	"gopkg.in/mgo.v2/bson"
)

const DB = "news"
const COL = "leTemps"
const size = 500
const start = 0

type Article struct {
	Id           bson.ObjectId `json:"id" bson:"_id,omitempty"`
	Title        string        `bson:"title"`
	Url          string        `bson:"url"`
	Html         string        `bson:"html"`
	ClearedHTML  string        `bson:"clearedHTML"`
	Source       string        `bson:"source"`
	CrawlData    uint          `bson:"crawlData"`
	ArticleDate  int64         `bson:"articleDate"`
	Tags         []string      `bson:"tag"`
	CleanContent string        `bson:"cleanContent"`
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
	}
	bulk.Run()
}

func process(article *Article, bulk *mgo.Bulk) {
	cmd := exec.Command("python3", "processLine.py")
	cmd.Stdin = strings.NewReader(article.ClearedHTML)
	var out bytes.Buffer
	cmd.Stdout = &out
	err := cmd.Run()
	if err != nil {
		log.Fatal(err)
	}
	cleanContent := out.String()
	article.CleanContent = cleanContent
	// fmt.Println("Before:")
	// fmt.Println(article.ClearedHTML)
	// fmt.Println("Result: ")
	// fmt.Println(cleanContent)
	bulk.Update(bson.M{"_id": article.Id}, bson.M{"$set": bson.M{"cleanContent": article.CleanContent}})
}

func batch(session *mgo.Session, start int, size int) []*Article {
	var articles []*Article
	session.DB(DB).C(COL).Find(bson.M{}).Skip(start).Limit(size).All(&articles)
	return articles
}
