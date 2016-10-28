package eu.lucazanini.arpav.xml;

public class Previsione {

    public final static String TAG_DATA_EMISSIONE = "data_emissione";
    public final static String TAG_DATA_AGGIORNAMENTO = "data_aggiornamento";
    public final static String TAG_BOLLETTINO = "bollettino";

    public final static String ATTR_DATA = "date";

    private String dataEmissione, dataAggiornamento;
    private Bollettino meteoVeneto;

    public String getDataAggiornamento() {
        return dataAggiornamento;
    }

    public void setDataAggiornamento(String dataAggiornamento) {
        this.dataAggiornamento = dataAggiornamento;
    }

    public String getDataEmissione() {
        return dataEmissione;
    }

    public void setDataEmissione(String dataEmissione) {
        this.dataEmissione = dataEmissione;
    }

    public void setMeteoVeneto() {
        meteoVeneto = new Bollettino();
    }

    public Bollettino newMeteoVeneto() {
        meteoVeneto = new Bollettino();
        return meteoVeneto;
    }

    public Bollettino getMeteoVeneto() {
        return meteoVeneto;
    }

    public class Bollettino {

        public final static String ATTR_BOLLETTINO_ID = "bollettinoid";
        public final static String ATTR_NOME = "name";
        public final static String ATTR_TITOLO = "title";
        public final static String TAG_EVOLUZIONE_GENERALE = "evoluzionegenerale";
        public final static String TAG_AVVISO = "avviso";
        public final static String TAG_FENOMENI_PARTICOLARI = "fenomeniparticolari";
        public final static String TAG_GIORNO = "giorno";

        public final static String METEO_VENETO = "MV";

        private String bollettinoId, nome, titolo, evoluzioneGenerale, avviso, fenomeniParticolari;
        private Giorno[] giorni = new Giorno[5];

        public String getBollettinoId() {
            return bollettinoId;
        }

        public void setBollettinoId(String bollettinoId) {
            this.bollettinoId = bollettinoId;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getTitolo() {
            return titolo;
        }

        public void setTitolo(String titolo) {
            this.titolo = titolo;
        }

        public String getEvoluzioneGenerale() {
            return evoluzioneGenerale;
        }

        public void setEvoluzioneGenerale(String evoluzioneGenerale) {
            this.evoluzioneGenerale = evoluzioneGenerale;
        }

        public String getAvviso() {
            return avviso;
        }

        public void setAvviso(String avviso) {
            this.avviso = avviso;
        }

        public String getFenomeniParticolari() {
            return fenomeniParticolari;
        }

        public void setFenomeniParticolari(String fenomeniParticolari) {
            this.fenomeniParticolari = fenomeniParticolari;
        }

        public Giorno[] getGiorni() {
            return giorni;
        }

        public void setGiorno(Giorno[] giorni) {
            this.giorni = giorni;
        }

        public Giorno newGiorno() {
            for (int i = 0; i < giorni.length; i++) {
                if (giorni[i] == null) {
                    giorni[i] = new Giorno();
                    return giorni[i];
                }
            }
            return null;
        }

        public class Giorno {

            public final static String ATTR_DATA = "data";
            public final static String TAG_IMMAGINE = "img";
            public final static String ATTR_SORGENTE = "src";
            public final static String ATTR_DIDASCALIA = "caption";
            public final static String TAG_TESTO = "text";

            private String data, sorgente, didascalia, testo;

            public String getData() {
                return data;
            }

            public void setData(String data) {
                this.data = data;
            }

            public String getSorgente() {
                return sorgente;
            }

            public void setSorgente(String sorgente) {
                this.sorgente = sorgente;
            }

            public String getDidascalia() {
                return didascalia;
            }

            public void setDidascalia(String didascalia) {
                this.didascalia = didascalia;
            }

            public String getTesto() {
                return testo;
            }

            public void setTesto(String testo) {
                this.testo = testo;
            }
        }

    }

}
