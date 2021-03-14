const express = require('express');
const port = 8888;
const app = express();
const bodyParser = require('body-parser');
const fetch = require('node-fetch');

const jsdom = require("jsdom");
const { JSDOM } = jsdom;


app.use(bodyParser.json())



 function calculateWaste(urun) {
  asilUrun = urun;
  urun = urun.toLowerCase();
  urun = urun.replace(/ş/g, 's');
  urun = urun.replace(/ğ/g, 'g');
  urun = urun.replace(/ç/g, 'c');
  urun = urun.replace(/ı/g, 'i');
  urun = urun.replace(/ö/g, 'o');
  urun = urun.replace(/ü/g, 'u');

  console.log(urun);
  var database = [
  {
    'urun':'ekmek',
    'su':'648',
    'co2':'1.6'
  },
  {
    'urun':'pirinc',
    'su':'2248',
    'co2':'4.5'
  },
  {
    'urun':'seker',
    'su':'218',
    'co2':'1.8'
  },
  {
    'urun':'aycicek yag',
    'su':'1008',
    'co2':'3.6'
  },
  {
    'urun':'zeytin yag',
    'su':'2142',
    'co2':'5.4'
  },
  {
    'urun':'kahve',
    'su':'26',
    'co2':'28.5'
  },
  {
    'urun':'cikolata',
    'su':'541',
    'co2':'46.7'
  },
  {
    'urun':'et',
    'su':'1451',
    'co2':'99.5'
  },
  {
    'urun':'sut',
    'su':'628',
    'co2':'3.2'
  },
  {
    'urun':'peynir',
    'su':'5605',
    'co2':'23.9'
  },
  {
    'urun':'yumurta',
    'su':'578',
    'co2':'4.7'
  },
  {
    'urun':'balik',
    'su':'3691',
    'co2':'13.6'
  },
  ];
  for (var j=0; j<database.length; j++){
    i = database[j];
    if (urun.search(i["urun"]) != -1) {
      res="{'su':'"+i['su']+"', 'co2':'"+i['co2']+"','urun':'"+asilUrun+"'}";
      return res
    }
  }
  return "{'su':'NULL','co2':'NULL', 'urun':'NULL'}";
}

app.get('/barkod/:barkod', (request, response)=> {
  var barkod = request.params.barkod;

    fetch('https://barkodoku.com/'+barkod)
    .then(resp=> resp.text()).then(body =>{
      var dom = new JSDOM(body);
      urun = dom.window.document.querySelector("#form1 > div > div > section:nth-child(3) > article:nth-child(1) > div.col-xs-24.col-sm-24.col-md-9.excerpet > h3 > a").textContent;

      waste =  calculateWaste(urun);
      console.log(waste);
      response.send(waste);
    }).catch((error)=>{console.log(error); response.send("{'su':'NULL','co2':'NULL'}");}); 
});

app.listen(port);
console.log("Listening");






