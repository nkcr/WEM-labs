import sys
#import nltk
from nltk.tokenize import word_tokenize
#nltk.download('punkt')
from nltk.stem.snowball import SnowballStemmer
stemmerFR = SnowballStemmer("french")
stemmerEN = SnowballStemmer("english")
import re
from scipy import optimize

alphaOnly = lambda w: not w.replace('"', '').replace("'", '')\
          .replace(".", '').replace(",", '').isnumeric()

gt2 = lambda w: len(w) > 2

ppFilters = lambda w: alphaOnly(w) and gt2(w)

def clean_str(string):
  """Original taken
  from https://github.com/yoonkim/CNN_sentence/blob/master/process_data.py
  """
  string = re.sub(r"[^A-Za-z0-9()èéàÉÀêÊçÇ]", " ", string)
  string = re.sub(r"\'s", " \'s", string)
  string = re.sub(r"\'ve", " \'ve", string)
  string = re.sub(r"n\'t", " n\'t", string)
  string = re.sub(r"\'re", " \'re", string)
  string = re.sub(r"\'d", " \'d", string)
  string = re.sub(r"\'ll", " \'ll", string)
  string = re.sub(r",", " , ", string)
  string = re.sub(r"!", " ! ", string)
  string = re.sub(r"\(", " \( ", string)
  string = re.sub(r"\)", " \) ", string)
  string = re.sub(r"\?", " \? ", string)
  string = re.sub(r"\s{2,}", " ", string)
  return string.strip().lower()


def ppMaps(w):
  #w = clean_str(w)
  w = stemmerFR.stem(w)
  #w = stemmerEN.stem(w)
  return w

def preprocessing(tokens):
  tokens = list(filter(ppFilters, map(ppMaps, tokens)))
  return tokens

def process_line(line):
  return " ".join(preprocessing(word_tokenize(clean_str(line))))

for line in sys.stdin:
  sys.stdout.write(process_line(line))
