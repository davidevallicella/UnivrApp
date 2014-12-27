package com.cellasoft.univrapp.model;

import com.cellasoft.univrapp.R;
import com.cellasoft.univrapp.UnivrReaderFactory;
import com.cellasoft.univrapp.reader.UnivrReader;
import com.cellasoft.univrapp.utils.LecturersHandler;
import com.cellasoft.univrapp.utils.Lists;
import com.cellasoft.univrapp.widget.ContactItemInterface;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Department {

    public boolean updating = false;
    public String name;
    public String url;
    public String domain;
    public int dest;
    public int logo_from_resource;
    private Object synRoot = new Object();

    public Department() {
        this.dest = 0;
    }

    public Department(String name) {
        this.name = name;
        this.dest = Departments.DIP_DEST.get(name);
        this.domain = Departments.DIP_DOMAIN.get(name);
        this.url = Departments.URL.get(name);
        this.logo_from_resource = Departments.LOGO.get(name);
    }

    public Department(Department department) {
        this.name = department.name;
        this.dest = department.dest;
        this.domain = department.domain;
        this.url = department.url;
        this.logo_from_resource = department.logo_from_resource;
    }

    public static Department getUniversityByDest(int dest) {
        Department department;

        switch (dest) {
            case Departments.DEST_DIP_BIOTECNOLOGIE:
                department = new Department(Departments.DIP_BIOTECNOLOGIE);
                break;
            case Departments.DEST_DIP_CHIRURGIA:
                department = new Department(Departments.DIP_CHIRURGIA);
                break;
            case Departments.DEST_DIP_ECONOMIA_AZIENDALE:
                department = new Department(Departments.DIP_ECONOMIA_AZIENDALE);
                break;
            case Departments.DEST_DIP_FILOL_LET_LING:
                department = new Department(Departments.DIP_FILOL_LET_LING);
                break;
            case Departments.DEST_DIP_FILOS_PEDA_PSICO:
                department = new Department(Departments.DIP_FILOS_PEDA_PSICO);
                break;
            case Departments.DEST_DIP_INFORMATICA:
                department = new Department(Departments.DIP_INFORMATICA);
                break;
            case Departments.DEST_DIP_LING_LETTERE:
                department = new Department(Departments.DIP_LING_LETTERE);
                break;
            case Departments.DEST_DIP_MEDICINA:
                department = new Department(Departments.DIP_MEDICINA);
                break;
            case Departments.DEST_DIP_PATO_DIAGN:
                department = new Department(Departments.DIP_PATO_DIAGN);
                break;
            case Departments.DEST_DIP_SANITA_MEDI_COM:
                department = new Department(Departments.DIP_SANITA_MEDI_COM);
                break;
            case Departments.DEST_DIP_SCIENZE_DELLA_VITA:
                department = new Department(Departments.DIP_SCIENZE_DELLA_VITA);
                break;
            case Departments.DEST_DIP_SCIENZE_ECON:
                department = new Department(Departments.DIP_SCIENZE_ECON);
                break;
            case Departments.DEST_DIP_SCIENZE_GIURIDICHE:
                department = new Department(Departments.DIP_SCIENZE_GIURIDICHE);
                break;
            case Departments.DEST_DIP_SCIENZE_NEUROLOGICHE:
                department = new Department(Departments.DIP_SCIENZE_NEUROLOGICHE);
                break;
            case Departments.DEST_DIP_TEMPO_IMMAGINE:
                department = new Department(Departments.DIP_TEMPO_IMMAGINE);
                break;
            default:
                department = new Department();
                break;
        }

        return department;
    }

    public static List<Department> getAllUniversity() {
        List<Department> universities = Lists.newArrayList();
        universities
                .add(getUniversityByDest(Departments.DEST_DIP_BIOTECNOLOGIE));
        universities.add(getUniversityByDest(Departments.DEST_DIP_CHIRURGIA));
        universities
                .add(getUniversityByDest(Departments.DEST_DIP_ECONOMIA_AZIENDALE));
        universities
                .add(getUniversityByDest(Departments.DEST_DIP_FILOL_LET_LING));
        universities
                .add(getUniversityByDest(Departments.DEST_DIP_FILOS_PEDA_PSICO));
        universities.add(getUniversityByDest(Departments.DEST_DIP_INFORMATICA));
        universities
                .add(getUniversityByDest(Departments.DEST_DIP_LING_LETTERE));
        universities.add(getUniversityByDest(Departments.DEST_DIP_MEDICINA));

        universities.add(getUniversityByDest(Departments.DEST_DIP_PATO_DIAGN));
        universities
                .add(getUniversityByDest(Departments.DEST_DIP_SANITA_MEDI_COM));
        universities
                .add(getUniversityByDest(Departments.DEST_DIP_SCIENZE_DELLA_VITA));
        universities
                .add(getUniversityByDest(Departments.DEST_DIP_SCIENZE_ECON));
        universities
                .add(getUniversityByDest(Departments.DEST_DIP_SCIENZE_GIURIDICHE));
        universities
                .add(getUniversityByDest(Departments.DEST_DIP_SCIENZE_NEUROLOGICHE));
        universities
                .add(getUniversityByDest(Departments.DEST_DIP_TEMPO_IMMAGINE));
        return universities;
    }

    public List<ContactItemInterface> update() {
        synchronized (synRoot) {
            if (updating)
                return null;
            updating = true;
        }

        List<ContactItemInterface> lecturers = updateLecturers();

        List<ContactItemInterface> newItems = saveLecturers(lecturers);

        synchronized (synRoot) {
            updating = false;
        }

        return newItems;
    }

    private List<ContactItemInterface> updateLecturers() {
        List<ContactItemInterface> lecturers = Lists.newArrayList();
        try {
            UnivrReader reader = UnivrReaderFactory.getUnivrReader();

            lecturers = reader.executeGetJSON(this, new LecturersHandler());

        } catch (Exception e) {
            // LOGE("ERROR", e.getMessage());
        }
        return lecturers;
    }

    private List<ContactItemInterface> saveLecturers(
            List<ContactItemInterface> lecturers) {
        List<ContactItemInterface> newItems = Lists.newArrayList();
        if (lecturers != null && !lecturers.isEmpty()) {

            for (ContactItemInterface lecturer : lecturers) {
                if (((Lecturer) lecturer).save()) {
                    newItems.add(lecturer);
                }
            }

        }

        return newItems;
    }

    public static final class Departments {
        // Univeristes RSS post data
        public static final String RSS_POST_DATA = "/?ent=avviso&rss=0&dest=";
        // Universites name
        public static final String DIP_INFORMATICA = "Informatica";
        public static final String DIP_BIOTECNOLOGIE = "Biotecnologie";
        public static final String DIP_CHIRURGIA = "Chirurgia";
        public static final String DIP_ECONOMIA_AZIENDALE = "Economia Aziendale";
        public static final String DIP_FILOL_LET_LING = "Filologia, Letteratura e Linguistica";
        public static final String DIP_FILOS_PEDA_PSICO = "Filosofia, Pedagogia e Psicologia";
        public static final String DIP_LING_LETTERE = "Lingue e Letterature Straniere";
        public static final String DIP_MEDICINA = "Medicina";
        public static final String DIP_PATO_DIAGN = "Patologia e Diagnostica";
        public static final String DIP_SANITA_MEDI_COM = "Sanità Pubblica e Medicina di Comunità";
        public static final String DIP_SCIENZE_ECON = "Scienze Economiche";
        public static final String DIP_SCIENZE_DELLA_VITA = "Scienze della Vita e della Riproduzione";
        public static final String DIP_SCIENZE_GIURIDICHE = "Scienze Giuridiche";
        public static final String DIP_SCIENZE_NEUROLOGICHE = "Scienze Neurologiche e del Movimento";
        public static final String DIP_TEMPO_IMMAGINE = "Tempo, Spazio, Immagine, Società";

        public static final int DEST_DIP_INFORMATICA = 165;
        public static final int DEST_DIP_BIOTECNOLOGIE = 171;
        public static final int DEST_DIP_CHIRURGIA = 212;
        public static final int DEST_DIP_ECONOMIA_AZIENDALE = 179;
        public static final int DEST_DIP_FILOL_LET_LING = 237;
        public static final int DEST_DIP_FILOS_PEDA_PSICO = 222;
        public static final int DEST_DIP_LING_LETTERE = 227;
        public static final int DEST_DIP_MEDICINA = 207;
        public static final int DEST_DIP_PATO_DIAGN = 194;
        public static final int DEST_DIP_SANITA_MEDI_COM = 217;
        public static final int DEST_DIP_SCIENZE_ECON = 175;
        public static final int DEST_DIP_SCIENZE_DELLA_VITA = 189;
        public static final int DEST_DIP_SCIENZE_GIURIDICHE = 184;
        public static final int DEST_DIP_SCIENZE_NEUROLOGICHE = 200;
        public static final int DEST_DIP_TEMPO_IMMAGINE = 232;

        public static final Map<String, String> DIP_DOMAIN;

        static {
            DIP_DOMAIN = new HashMap<String, String>();
            DIP_DOMAIN.put(DIP_INFORMATICA, "http://www.di.univr.it");
            DIP_DOMAIN.put(DIP_BIOTECNOLOGIE, "http://www.dbt.univr.it");
            DIP_DOMAIN.put(DIP_CHIRURGIA, "http://www.dc.univr.it");
            DIP_DOMAIN.put(DIP_ECONOMIA_AZIENDALE, "http://www.dea.univr.it");
            DIP_DOMAIN.put(DIP_FILOL_LET_LING, "http://www.dfll.univr.it");
            DIP_DOMAIN.put(DIP_FILOS_PEDA_PSICO, "http://www.dfpp.univr.it");
            DIP_DOMAIN.put(DIP_LING_LETTERE, "http://www.dlls.univr.it");
            DIP_DOMAIN.put(DIP_MEDICINA, "http://www.dm.univr.it");
            DIP_DOMAIN.put(DIP_PATO_DIAGN, "http://www.dpd.univr.it");
            DIP_DOMAIN.put(DIP_SANITA_MEDI_COM, "http://www.dspmc.univr.it");
            DIP_DOMAIN.put(DIP_SCIENZE_ECON, "http://www.dse.univr.it");
            DIP_DOMAIN.put(DIP_SCIENZE_DELLA_VITA, "http://www.dsvr.univr.it");
            DIP_DOMAIN.put(DIP_SCIENZE_GIURIDICHE, "http://www.dsg.univr.it");
            DIP_DOMAIN
                    .put(DIP_SCIENZE_NEUROLOGICHE, "http://www.dsnm.univr.it");
            DIP_DOMAIN.put(DIP_TEMPO_IMMAGINE, "http://www.dtesis.univr.it");
        }

        public static final Map<String, Integer> DIP_DEST;

        static {
            DIP_DEST = new HashMap<String, Integer>();
            DIP_DEST.put(DIP_INFORMATICA, DEST_DIP_INFORMATICA);
            DIP_DEST.put(DIP_BIOTECNOLOGIE, DEST_DIP_BIOTECNOLOGIE);
            DIP_DEST.put(DIP_CHIRURGIA, DEST_DIP_CHIRURGIA);
            DIP_DEST.put(DIP_ECONOMIA_AZIENDALE, DEST_DIP_ECONOMIA_AZIENDALE);
            DIP_DEST.put(DIP_FILOL_LET_LING, DEST_DIP_FILOL_LET_LING);
            DIP_DEST.put(DIP_FILOS_PEDA_PSICO, DEST_DIP_FILOS_PEDA_PSICO);
            DIP_DEST.put(DIP_LING_LETTERE, DEST_DIP_LING_LETTERE);
            DIP_DEST.put(DIP_MEDICINA, DEST_DIP_MEDICINA);
            DIP_DEST.put(DIP_PATO_DIAGN, DEST_DIP_PATO_DIAGN);
            DIP_DEST.put(DIP_SANITA_MEDI_COM, DEST_DIP_SANITA_MEDI_COM);
            DIP_DEST.put(DIP_SCIENZE_ECON, DEST_DIP_SCIENZE_ECON);
            DIP_DEST.put(DIP_SCIENZE_DELLA_VITA, DEST_DIP_SCIENZE_DELLA_VITA);
            DIP_DEST.put(DIP_SCIENZE_GIURIDICHE, DEST_DIP_SCIENZE_GIURIDICHE);
            DIP_DEST.put(DIP_SCIENZE_NEUROLOGICHE,
                    DEST_DIP_SCIENZE_NEUROLOGICHE);
            DIP_DEST.put(DIP_TEMPO_IMMAGINE, DEST_DIP_TEMPO_IMMAGINE);
        }

        public static final Map<String, Integer> LOGO;

        static {
            LOGO = new HashMap<String, Integer>();
            LOGO.put(DIP_BIOTECNOLOGIE, R.drawable.logo_bio);
            LOGO.put(DIP_CHIRURGIA, R.drawable.univr);
            LOGO.put(DIP_ECONOMIA_AZIENDALE, R.drawable.univr);
            LOGO.put(DIP_FILOL_LET_LING, R.drawable.univr);
            LOGO.put(DIP_FILOS_PEDA_PSICO, R.drawable.univr);
            LOGO.put(DIP_INFORMATICA, R.drawable.logo_info);
            LOGO.put(DIP_LING_LETTERE, R.drawable.univr);
            LOGO.put(DIP_MEDICINA, R.drawable.univr);
            LOGO.put(DIP_PATO_DIAGN, R.drawable.univr);
            LOGO.put(DIP_SANITA_MEDI_COM, R.drawable.univr);
            LOGO.put(DIP_SCIENZE_DELLA_VITA, R.drawable.univr);
            LOGO.put(DIP_SCIENZE_ECON, R.drawable.logo_eco);
            LOGO.put(DIP_SCIENZE_GIURIDICHE, R.drawable.univr);
            LOGO.put(DIP_SCIENZE_NEUROLOGICHE, R.drawable.logo_neuro);
            LOGO.put(DIP_TEMPO_IMMAGINE, R.drawable.univr);
        }

        public static final Map<String, String> URL;

        static {
            URL = new HashMap<String, String>();
            URL.put(DIP_BIOTECNOLOGIE, DIP_DOMAIN.get(DIP_BIOTECNOLOGIE)
                    + RSS_POST_DATA + DEST_DIP_BIOTECNOLOGIE);
            URL.put(DIP_CHIRURGIA, DIP_DOMAIN.get(DIP_CHIRURGIA)
                    + RSS_POST_DATA + DEST_DIP_CHIRURGIA);
            URL.put(DIP_ECONOMIA_AZIENDALE,
                    DIP_DOMAIN.get(DIP_ECONOMIA_AZIENDALE) + RSS_POST_DATA
                            + DEST_DIP_ECONOMIA_AZIENDALE);
            URL.put(DIP_FILOL_LET_LING, DIP_DOMAIN.get(DIP_FILOL_LET_LING)
                    + RSS_POST_DATA + DEST_DIP_FILOL_LET_LING);
            URL.put(DIP_FILOS_PEDA_PSICO, DIP_DOMAIN.get(DIP_FILOS_PEDA_PSICO)
                    + RSS_POST_DATA + DEST_DIP_FILOS_PEDA_PSICO);
            URL.put(DIP_INFORMATICA, DIP_DOMAIN.get(DIP_INFORMATICA)
                    + RSS_POST_DATA + DEST_DIP_INFORMATICA);
            URL.put(DIP_LING_LETTERE, DIP_DOMAIN.get(DIP_LING_LETTERE)
                    + RSS_POST_DATA + DEST_DIP_LING_LETTERE);
            URL.put(DIP_MEDICINA, DIP_DOMAIN.get(DIP_MEDICINA) + RSS_POST_DATA
                    + DEST_DIP_MEDICINA);
            URL.put(DIP_PATO_DIAGN, DIP_DOMAIN.get(DIP_PATO_DIAGN)
                    + RSS_POST_DATA + DEST_DIP_PATO_DIAGN);
            URL.put(DIP_SANITA_MEDI_COM, DIP_DOMAIN.get(DIP_SANITA_MEDI_COM)
                    + RSS_POST_DATA + DEST_DIP_SANITA_MEDI_COM);
            URL.put(DIP_SCIENZE_DELLA_VITA,
                    DIP_DOMAIN.get(DIP_SCIENZE_DELLA_VITA) + RSS_POST_DATA
                            + DEST_DIP_SCIENZE_DELLA_VITA);
            URL.put(DIP_SCIENZE_ECON, DIP_DOMAIN.get(DIP_SCIENZE_ECON)
                    + RSS_POST_DATA + DEST_DIP_SCIENZE_ECON);
            URL.put(DIP_SCIENZE_GIURIDICHE,
                    DIP_DOMAIN.get(DIP_SCIENZE_GIURIDICHE) + RSS_POST_DATA
                            + DEST_DIP_SCIENZE_GIURIDICHE);
            URL.put(DIP_SCIENZE_NEUROLOGICHE,
                    DIP_DOMAIN.get(DIP_SCIENZE_NEUROLOGICHE) + RSS_POST_DATA
                            + DEST_DIP_SCIENZE_NEUROLOGICHE);
            URL.put(DIP_TEMPO_IMMAGINE, DIP_DOMAIN.get(DIP_TEMPO_IMMAGINE)
                    + RSS_POST_DATA + DEST_DIP_TEMPO_IMMAGINE);
        }

        static String dip_requestURL = "/?ent=persona&grp=2";
        static String dip_lecturer_url = "/?ent=persona&id=";
    }
}
