package com.cellasoft.univrapp.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.cellasoft.univrapp.activity.R;

public class Constants {
	public static final int MAX_ITEMS = 15;
	public static final int MAX_ITEMS_PER_FETCH = 20;
	public static final int UPDATE_UI = 0;
	public static final int ERROR_MESSAGE = 1;
	public static final int NOTIFICATION_ID = 2;
	public static final String CHANNEL_ID = "id";
	public static final boolean DEBUG_MODE = true;
	public static final String LOG_TAG = "UnivrApp";
	public static final String RSS_PARAMS = "/fol/main?ent=avviso&rss=1&dest=";
	public static final String SENDER_ID = "318564088573";
	public static final String THUMB_UNIVR_URL = "http://www.univr.it/main?ent=pageaol&page=facolta";

	public static class UNIVERSITY {
		public static final String SCIENZE_MM_FF_NN = "Scienze matematiche fisiche e naturali";
		public static final String ECONOMIA = "Economia";
		public static final String SCIENZE_FORMAZIONE = "Scienze della formazione";
		public static final String GIURISPRUDENZA = "Giurisprudenza";
		public static final String LETTERE_FILOSOFIA = "Lettere e Filosofia";
		public static final String LINGUE = "Lingue e letteratre straniere";
		public static final String MEDICINA = "Medicina e Chirurgia";

		public static final int DEST_SCIENZE_MM_FF_NN = 1;
		public static final int DEST_ECONOMIA = 5;
		public static final int DEST_SCIENZE_FORMAZIONE = 9;
		public static final int DEST_GIURISPRUDENZA = 13;
		public static final int DEST_LETTERE_FILOSOFIA = 17;
		public static final int DEST_LINGUE = 21;
		public static final int DEST_MEDICINA = 25;

		private static Resources resources;

		static {
			resources = Application.getInstance().getResources();
		}
		
		public static final ArrayList<String> UNIVERSITES;
		static {
			UNIVERSITES = new ArrayList<String>();
			UNIVERSITES.add(ECONOMIA);
			UNIVERSITES.add(GIURISPRUDENZA);
			UNIVERSITES.add(LETTERE_FILOSOFIA);
			UNIVERSITES.add(LINGUE);
			UNIVERSITES.add(MEDICINA);
			UNIVERSITES.add(SCIENZE_FORMAZIONE);
			UNIVERSITES.add(SCIENZE_MM_FF_NN);
		}

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

		public static final Map<String, String> URL;
		static {
			URL = new HashMap<String, String>();
			URL.put(ECONOMIA, DOMAIN.get(ECONOMIA) + RSS_PARAMS + DEST_ECONOMIA);
			URL.put(GIURISPRUDENZA, DOMAIN.get(GIURISPRUDENZA) + RSS_PARAMS
					+ DEST_GIURISPRUDENZA);
			URL.put(LETTERE_FILOSOFIA, DOMAIN.get(LETTERE_FILOSOFIA)
					+ RSS_PARAMS + DEST_LETTERE_FILOSOFIA);
			URL.put(LINGUE, DOMAIN.get(LINGUE) + RSS_PARAMS + DEST_LINGUE);
			URL.put(MEDICINA, DOMAIN.get(MEDICINA) + RSS_PARAMS + DEST_MEDICINA);
			URL.put(SCIENZE_FORMAZIONE, DOMAIN.get(SCIENZE_FORMAZIONE)
					+ RSS_PARAMS + DEST_SCIENZE_MM_FF_NN);
			URL.put(SCIENZE_MM_FF_NN, DOMAIN.get(SCIENZE_MM_FF_NN) + RSS_PARAMS
					+ DEST_SCIENZE_MM_FF_NN);
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

		public static final Map<String, Drawable> LOGO;
		static {
			LOGO = new HashMap<String, Drawable>();
			LOGO.put(ECONOMIA, resources.getDrawable(R.drawable.eco_logo));
			LOGO.put(GIURISPRUDENZA, resources.getDrawable(R.drawable.giu_logo));
			LOGO.put(LETTERE_FILOSOFIA,
					resources.getDrawable(R.drawable.let_fil_logo));
			LOGO.put(LINGUE, resources.getDrawable(R.drawable.ling_logo));
			LOGO.put(MEDICINA, resources.getDrawable(R.drawable.med_logo));
			LOGO.put(SCIENZE_FORMAZIONE,
					resources.getDrawable(R.drawable.sci_form_logo));
			LOGO.put(SCIENZE_MM_FF_NN,
					resources.getDrawable(R.drawable.scie_logo));
		}

		public static final Map<String, Drawable> BANNER;
		static {
			BANNER = new HashMap<String, Drawable>();
			BANNER.put(SCIENZE_MM_FF_NN,
					resources.getDrawable(R.drawable.scienze_banner));

		}
	}
}
