package com.cellasoft.univrapp.utils;

import java.util.HashMap;
import java.util.Map;

public class Constants {
	public static final int MAX_ITEMS = 15;
	public static final int UPDATE_UI = 0;
	public static final int ERROR_MESSAGE = 1;
	public static final int NOTIFICATION_ID = 2;
	public static final String CHANNEL_ID = "id";
	public static final boolean DEBUG_MODE = true;
	public static final String LOG_TAG = "UnivrApp";

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
		
		public static final Map<String, String> URL;
		static {
			URL = new HashMap<String, String>();
			URL.put(ECONOMIA,
					"http://www.economia.univr.it/fol/main?ent=avviso&dest=5&rss=1");
			URL.put(GIURISPRUDENZA,
					"http://www.giurisprudenza.univr.it/fol/main?ent=avviso&dest=13&rss=1");
			URL.put(LETTERE_FILOSOFIA,
					"http://www.lettere.univr.it/fol/main?ent=avviso&dest=17&rss=1");
			URL.put(LINGUE,
					"http://www.lingue.univr.it/fol/main?ent=avviso&dest=21&rss=1");
			URL.put(MEDICINA,
					"http://www.medicina.univr.it/fol/main?ent=avviso&dest=25&rss=1");
			URL.put(SCIENZE_FORMAZIONE,
					"http://www.formazione.univr.it/fol/main?ent=avviso&dest=9&rss=1");
			URL.put(SCIENZE_MM_FF_NN,
					"http://www.scienze.univr.it/fol/main?ent=avviso&dest=1&rss=1");
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
	}
}
