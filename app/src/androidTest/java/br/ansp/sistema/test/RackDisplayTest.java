package br.ansp.sistema.test;

import android.test.ActivityInstrumentationTestCase2;
import br.ansp.sistema.RackDisplayActivity;

public class RackDisplayTest extends
		ActivityInstrumentationTestCase2<RackDisplayActivity> {

	public RackDisplayTest() {
		super("br.ansp.sistema", RackDisplayActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		RackDisplayActivity rda = getActivity();
	}

	public void testSearch() {
		
	}
}
