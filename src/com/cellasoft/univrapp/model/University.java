package com.cellasoft.univrapp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.provider.BaseColumns;

import com.cellasoft.univrapp.activity.R;

public class University {

	private static final String UNIVRAPP_URL = "http://wscunivrapp.cellasoft.cloudbees.net/";
	public String GET_LECTURERS_LIST_URL = UNIVRAPP_URL
			+ "json/lecturerService/lecturers?dest=";

	public String name;
	public String url;
	public String domain;
	public int dest;
	public int logo_from_resource;
	public int color_from_resource;

	public University() {
		this.dest = 0;
	}

	public University(String name) {
		this.name = name;
		this.dest = Universites.DEST.get(name);
		this.domain = Universites.DOMAIN.get(name);
		this.url = Universites.URL.get(name);
		this.logo_from_resource = Universites.LOGO.get(name);
		this.color_from_resource = Universites.COLOR.get(name);
		GET_LECTURERS_LIST_URL += dest;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public int getDest() {
		return dest;
	}

	public void setDest(int dest) {
		this.dest = dest;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getLogo_from_resource() {
		return logo_from_resource;
	}

	public void setLogo_from_resource(int logo_from_resource) {
		this.logo_from_resource = logo_from_resource;
	}

	public int getColor_from_resource() {
		return color_from_resource;
	}

	public void setColor_from_resource(int color_from_resource) {
		this.color_from_resource = color_from_resource;
	}

	public static University getUniversityByDest(int dest) {
		University university = null;

		switch (dest) {
		case Universites.DEST_ECONOMIA:
			university = new University(Universites.ECONOMIA);
			break;
		case Universites.DEST_GIURISPRUDENZA:
			university = new University(Universites.GIURISPRUDENZA);
			break;
		case Universites.DEST_LETTERE_FILOSOFIA:
			university = new University(Universites.LETTERE_FILOSOFIA);
			break;
		case Universites.DEST_LINGUE:
			university = new University(Universites.LINGUE);
			break;
		case Universites.DEST_MEDICINA:
			university = new University(Universites.MEDICINA);
			break;
		case Universites.DEST_SCIENZE_FORMAZIONE:
			university = new University(Universites.SCIENZE_FORMAZIONE);
			break;
		case Universites.DEST_SCIENZE_MM_FF_NN:
			university = new University(Universites.SCIENZE_MM_FF_NN);
			break;
		default:
			university = new University();
			break;
		}

		return university;
	}

	public static ArrayList<University> getAllUniversity() {
		ArrayList<University> universities = new ArrayList<University>();
		universities.add(getUniversityByDest(Universites.DEST_ECONOMIA));
		universities.add(getUniversityByDest(Universites.DEST_GIURISPRUDENZA));
		universities
				.add(getUniversityByDest(Universites.DEST_LETTERE_FILOSOFIA));
		universities.add(getUniversityByDest(Universites.DEST_LINGUE));
		universities.add(getUniversityByDest(Universites.DEST_MEDICINA));
		universities
				.add(getUniversityByDest(Universites.DEST_SCIENZE_FORMAZIONE));
		universities
				.add(getUniversityByDest(Universites.DEST_SCIENZE_MM_FF_NN));
		return universities;
	}

	public static final class Universites implements BaseColumns {
		// Univeristes RSS post data
		public static final String RSS_POST_DATA = "/fol/main?ent=avviso&rss=1&dest=";
		// Universites name
		public static final String SCIENZE_MM_FF_NN = "Scienze matematiche fisiche e naturali";
		public static final String ECONOMIA = "Economia";
		public static final String SCIENZE_FORMAZIONE = "Scienze della formazione";
		public static final String GIURISPRUDENZA = "Giurisprudenza";
		public static final String LETTERE_FILOSOFIA = "Lettere e Filosofia";
		public static final String LINGUE = "Lingue e letteratre straniere";
		public static final String MEDICINA = "Medicina e Chirurgia";
		// Universites dest
		public static final int DEST_SCIENZE_MM_FF_NN = 1;
		public static final int DEST_ECONOMIA = 5;
		public static final int DEST_SCIENZE_FORMAZIONE = 9;
		public static final int DEST_GIURISPRUDENZA = 13;
		public static final int DEST_LETTERE_FILOSOFIA = 17;
		public static final int DEST_LINGUE = 21;
		public static final int DEST_MEDICINA = 25;

		public static final Map<String, String> DOMAIN;
		static {
			DOMAIN = new HashMap<String, String>();
			DOMAIN.put(ECONOMIA, "http://www.economia.univr.it");
			DOMAIN.put(GIURISPRUDENZA, "http://www.giurisprudenza.univr.it");
			DOMAIN.put(LETTERE_FILOSOFIA, "http://www.lettere.univr.it");
			DOMAIN.put(LINGUE, "http://www.lingue.univr.it");
			DOMAIN.put(MEDICINA, "http://www.medicina.univr.it");
			DOMAIN.put(SCIENZE_FORMAZIONE, "http://www.formazione.univr.it");
			DOMAIN.put(SCIENZE_MM_FF_NN, "http://www.scienze.univr.it");
		}

		public static final Map<String, Integer> DEST;
		static {
			DEST = new HashMap<String, Integer>();
			DEST.put(ECONOMIA, DEST_ECONOMIA);
			DEST.put(GIURISPRUDENZA, DEST_GIURISPRUDENZA);
			DEST.put(LETTERE_FILOSOFIA, DEST_LETTERE_FILOSOFIA);
			DEST.put(LINGUE, DEST_LINGUE);
			DEST.put(MEDICINA, DEST_MEDICINA);
			DEST.put(SCIENZE_FORMAZIONE, DEST_SCIENZE_FORMAZIONE);
			DEST.put(SCIENZE_MM_FF_NN, DEST_SCIENZE_MM_FF_NN);
		}

		public static final Map<String, Integer> LOGO;
		static {
			LOGO = new HashMap<String, Integer>();
			LOGO.put(ECONOMIA, R.drawable.eco_logo);
			LOGO.put(GIURISPRUDENZA, R.drawable.giu_logo);
			LOGO.put(LETTERE_FILOSOFIA, R.drawable.let_fil_logo);
			LOGO.put(LINGUE, R.drawable.ling_logo);
			LOGO.put(MEDICINA, R.drawable.med_logo);
			LOGO.put(SCIENZE_FORMAZIONE, R.drawable.sci_form_logo);
			LOGO.put(SCIENZE_MM_FF_NN, R.drawable.scie_logo);
		}

		public static final Map<String, Integer> COLOR;
		static {
			COLOR = new HashMap<String, Integer>();
			COLOR.put(ECONOMIA, R.color.economia);
			COLOR.put(GIURISPRUDENZA, R.color.giurisprudenza);
			COLOR.put(LETTERE_FILOSOFIA, R.color.lettere_filosofia);
			COLOR.put(LINGUE, R.color.lingue);
			COLOR.put(MEDICINA, R.color.medicina);
			COLOR.put(SCIENZE_FORMAZIONE, R.color.scienze_formazione);
			COLOR.put(SCIENZE_MM_FF_NN, R.color.scineze_mm_ff_nn);
		}

		public static final Map<String, String> URL;
		static {
			URL = new HashMap<String, String>();
			URL.put(ECONOMIA, DOMAIN.get(ECONOMIA) + RSS_POST_DATA
					+ DEST_ECONOMIA);
			URL.put(GIURISPRUDENZA, DOMAIN.get(GIURISPRUDENZA) + RSS_POST_DATA
					+ DEST_GIURISPRUDENZA);
			URL.put(LETTERE_FILOSOFIA, DOMAIN.get(LETTERE_FILOSOFIA)
					+ RSS_POST_DATA + DEST_LETTERE_FILOSOFIA);
			URL.put(LINGUE, DOMAIN.get(LINGUE) + RSS_POST_DATA + DEST_LINGUE);
			URL.put(MEDICINA, DOMAIN.get(MEDICINA) + RSS_POST_DATA
					+ DEST_MEDICINA);
			URL.put(SCIENZE_FORMAZIONE, DOMAIN.get(SCIENZE_FORMAZIONE)
					+ RSS_POST_DATA + DEST_SCIENZE_MM_FF_NN);
			URL.put(SCIENZE_MM_FF_NN, DOMAIN.get(SCIENZE_MM_FF_NN)
					+ RSS_POST_DATA + DEST_SCIENZE_MM_FF_NN);
		}
	}
}
