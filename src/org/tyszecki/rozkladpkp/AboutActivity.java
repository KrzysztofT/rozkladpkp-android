package org.tyszecki.rozkladpkp;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItem;
import android.text.Html;
import android.text.util.Linkify;
import android.webkit.WebView;
import android.widget.TextView;

public class AboutActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		WebView v = new WebView(this);
		setContentView(v);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		v.loadDataWithBaseURL(null,"<h1>Rozkład PKP</h1>" +
				"<p>Rozkład PKP jest nieoficjalną aplikacją prezentującą rozkład połączeń kolejowych w oparciu o system informacji <a href=\"http://rozklad.sitkol.pl\">SITKOL</a>." +
				"Oprócz rozkładów i połączeń, aplikacja wyświetla bieżące komunikaty o utrudnieniach w ruchu, trasy pociągów, ceny połączeń i opóźnienia, " +
				"o ile informacje te są dostępne." +
				"W przeciwieństwie do konkurencyjnych aplikacji, nie pobiera strony internetowej z rozkładem, dzięki czemu działa szybciej i pobiera mniej danych.</p> " +
				"<h2>Kontakt</h2>" +
				"<p>Jeśli chcesz zgłosić uwagi, błędy w programie lub propozycje zmian, skontaktuj się ze mną poprzez adres e-mail <a href=\"mailto:krzysztof@tyszecki.org\">krzysztof@tyszecki.org</a></p>" +
				"<h2>Wsparcie</h2>" +
				"<p>Aplikacja pozostanie darmowa i pozbawiona reklam - jeśli jednak chcesz przekazać darowiznę na jej rozwój, możesz to zrobić poprzez system PayPal, wpłacając darowiznę na adres <a href=\"https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=7R9PCLE2FCDXU\">krzysztof@tyszecki.org</a></p>",
				"text/html","UTF8",null);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case android.R.id.home:
            // app icon in action bar clicked; go home
            /*Intent intent = new Intent(this, RozkladPKP.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);*/
        	finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
    }
	}
}
