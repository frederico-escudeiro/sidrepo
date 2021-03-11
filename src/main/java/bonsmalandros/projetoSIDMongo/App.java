package bonsmalandros.projetoSIDMongo;

public class App
{

	public static void main( String[] args )
	{
		LeituraMedicoes t1 = new LeituraMedicoes("sensorh2", "sensorh2",1000*60*60*24*9, 1000*60*5, 200);
		//LeituraMedicoes t2 = new LeituraMedicoes("sensorh2", "sensorh2");
		t1.start();
		//t2.start();
	}
}
