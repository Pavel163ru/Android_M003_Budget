package ru.razoom.develop.budget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author Pavel
 *Программа для расчета дополнительных отчислений с зарплаты и запись 
 *этих параметров в список
 *
 *12.07.2014 v0.1(1)
 *	-первая полностью рабочая версия, проверено на эмуляторе
 *
 *13.07.2014 v0.1(2)
 *	-исправлен баг при первом запуске приложения
 *
 *14.07.2014 v0.1(3)
 *	-константы перенесены в интерфейс BudgetConstants
 *
 *31.07.2014
 *	-введена часть констант
 *	-!метод сохранения всего списка не доделан
 *	-!!! сделать удаление любого пункта, с последующим сохранением всего списка
 *	
 *02.08.2014 v0.2(4)
 *	-удаление любого пункта
 *	-сохранение списка с исключением
 *	-сортировка списка по дате
 *	-дополнительный массив time для сортировки
 *	-перевод даты из String в числовой вид int array_date_time(исправлен баг)
 *	-Всплывающее сообщение об ошибке удаления и редактирования
 *	-поддержка данных прошлой версии
 *	-!авто заполнение даты, работает, пока отключено
 *
 */
//подключаем интерфейс слушателя нажатий на список
public class MainActivity extends Activity implements OnItemClickListener, BudgetConstants {
	
	
	ListView lvMain;
		
	int array_salari[] = new int[50]; //{12000, 11000, 13000, 11275, 10843, 13967,0};
	int array_advance[] = new int[50]; //{7000, 6000, 7500, 2550, 8300, 8500,0};
	int all_pay;
	
	
	
	int position = 0; //позиция пункта в Массивах
	int length; //Длина таблицы
	
	String array_date[] = new String[50]; //{"03/2014", "04/2014","05/2014", "06/2014", "07/2014", "08/2014",""};
	int array_date_time[] = new int[50];
 	
	TextView text_pay, text_advance, text_bank, text_rent, 
			text_moneybox, text_rest, text_purse, text_all_pay, text_date;
	
	SharedPreferences spMemory;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Log.d("myLog","-------------Start--------------");
		
		load(); //загружаем данные в массивы
		
		//Получаем ссылки на TextView по Id для работы с текстом
		text_pay = (TextView) findViewById(R.id.text_pay);
		text_advance = (TextView) findViewById(R.id.text_advance);
		text_bank = (TextView) findViewById(R.id.text_bank);
		text_rent = (TextView) findViewById(R.id.text_rent);
		text_moneybox = (TextView) findViewById(R.id.text_moneybox);
		text_rest = (TextView) findViewById(R.id.text_rest);
		text_purse = (TextView) findViewById(R.id.text_purse);
		text_all_pay = (TextView) findViewById(R.id.text_all_pay);
		text_date = (TextView) findViewById(R.id.text_date);
		
	    lvMain = (ListView) findViewById(R.id.lvMain);

	    listOut();//обновляем Список
	    updateHead(length-1); //обновляем заголовок
	    
	    lvMain.setOnItemClickListener(this);//подключаем обработчик кликов
	}
	
	
	//Обработчик нажатий на список
	public void onItemClick(AdapterView<?> parent, View view, int position_, long id) {
		
		position = length - 1 - position_; //Расчитываем номер строки
		
		updateHead(position); //обновляем заголовок согласно выбранному пункту	  
    }
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item){
		Log.d(LOG,"click on menu: " + item.getItemId() + item.toString());
				
		switch (item.getItemId()){
		case R.id.itemExit://завершаем приложение
			finish();
			break;
		case R.id.itemNew://Запускаем пустое активити редактирования 					
			startActivityForResult(new Intent(this, AddActivity.class), POS_ADD);
			break;
		case R.id.itemEdit://Отправляем данные пункта для редактирования
			if(length < 1){
				Toast.makeText(this, "Ошибка редактирования", Toast.LENGTH_SHORT).show();
				break;
			}
			
			Intent intent = new Intent(this, AddActivity.class);
			
			intent.putExtra(CONST_SALARI, array_salari[position]);
			intent.putExtra(CONST_ADVANCE, array_advance[position]);
			intent.putExtra(CONST_DATE, array_date[position]);
			intent.putExtra(C.CONST_MODE, POS_EDIT);
						
			startActivityForResult(intent, POS_EDIT);
			break;	
		case R.id.itemDel:
			deletePosition(position);	
			listOut();
			updateHead(length-1);
			break;
		case R.id.itemSort:
			Log.d(C.LOG, "-list sort start");
			sortList();
			save();
			listOut();
			break;
		}
		
		return false;
	}
	
	//обрабатываем результат запуска второго активити
	protected void onActivityResult(int requestCode, int requestResult, Intent data){
		if (requestResult == RESULT_CANCELED){
			Log.d(C.LOG,"result canceled");
			return;
		}
		
		String aData = data.getStringExtra(CONST_DATE);
		Log.d(LOG,"Date "+aData+", Salari "+data.getIntExtra(CONST_SALARI, 0)
				+", Advance "+data.getIntExtra(CONST_ADVANCE, 0));
		
		switch (requestCode){
		case POS_ADD:
		
			length++;//увеличиваем длину списка
			
			array_salari[length-1] = data.getIntExtra(CONST_SALARI, 0);
			array_advance[length-1] = data.getIntExtra(CONST_ADVANCE, 0);
			array_date[length-1] = data.getStringExtra(CONST_DATE);
			//Вычисляем тайм для сортировки
			array_date_time[length-1] = parseDate(array_date[length-1]);
			
			savePosition(length); //Сохраняем в новой ячейке № length
			updateHead(length-1);
			
			break;
		case POS_EDIT:
			
			array_salari[position] = data.getIntExtra(CONST_SALARI, 0);
			array_advance[position] = data.getIntExtra(CONST_ADVANCE, 0);
			array_date[position] = data.getStringExtra(CONST_DATE);
			array_date_time[position] = parseDate(array_date[position]);
			
			Log.d(C.LOG,"Edit position #"+position);
					
			savePosition(position+1);
			updateHead(position);
			break;
		}
		
		listOut();
	}
	
	//Создаем обратный список, последняя позиция в верху, и выводим на экран
	void listOut(){
		// упаковываем данные в понятную для адаптера структуру
	    ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(length);
	    
	    Map<String, Object> m;
	    
	    //Добавляем пункты
	    for (int i = (length-1); i >= 0; i--) {
	    	//Log.d(C.LOG,"list position create #"+i);
	    	m = new HashMap<String, Object>();
		    m.put(CONST_SALARI, "Зарплата:" + array_salari[i]);
		    m.put(CONST_ADVANCE, "Аванс:" + array_advance[i]);
		    m.put(CONST_DATE, array_date[i]);
		    all_pay = array_advance[i]+array_salari[i];
		    m.put(CONST_ALL_PAY, "Общая:" + all_pay);
		    data.add(m);
		    
		    //Log.d(C.LOG, "list element #"+(i+1)+": s"+array_salari[i]+" a"+array_advance[i]+" d"+array_date[i]+", t"+array_date_time[i]);
	    }
	    
	    // массив имен атрибутов, из которых будут читаться данные
	    String[] from = { CONST_SALARI, CONST_ADVANCE, CONST_DATE, CONST_ALL_PAY };
	    // массив ID View-компонентов, в которые будут вставлять данные
	    int[] to = { R.id.textView2, R.id.textView3, R.id.textView1, R.id.textView4 };
	    
	    // создаем адаптер
	    SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.list, from, to);
	    
	    // определяем список и присваиваем ему адаптер
	    lvMain.setAdapter(sAdapter);  
		
	}
	@Deprecated
	// Старый метод, теперь позицию соранения можно выбирать, в новом методе
	void saveLastPosition(){
		spMemory = getPreferences(MODE_PRIVATE);
		Editor ed = spMemory.edit();
		ed.putInt("length", length);
		ed.putInt("salari"+length, array_salari[length-1]);
		ed.putInt("advance"+length, array_advance[length-1]);
		ed.putString("date"+length, array_date[length-1]);
		ed.commit();
	}

	//Метод позволяетc сохранить(перезаписать) данные в выбранной строке (savePosition);
	void savePosition(int savePosition){
		spMemory = getPreferences(MODE_PRIVATE);
		Editor ed = spMemory.edit();
		ed.putInt(C.CONST_LENGTH, length);
		ed.putInt(C.CONST_SALARI+savePosition, array_salari[savePosition-1]);
		ed.putInt(C.CONST_ADVANCE+savePosition, array_advance[savePosition-1]);
		ed.putString(C.CONST_DATE+savePosition, array_date[savePosition-1]);
		
		ed.putInt(C.CONST_TIME+savePosition, array_date_time[savePosition-1]);
		
		ed.commit();
	}
	
	//метор сохраняет весь список, используя исключение(salari == -1)
	void save(){
		spMemory = getPreferences(MODE_PRIVATE);
		Editor ed = spMemory.edit();
		ed.putInt(C.CONST_LENGTH, length);
		
		int count = 0;//для перебора массива
		for (int i=1; i<=length; i++){//номер записи от 1 до length
			switch (array_salari[count]){
			case (-1):
				count++;//если знак исключения строки, переключить массив
				Log.d(C.LOG, "exception salary = -1, skip row # "+count);
			default://в предыдущем блоке нет break, т.к. этот должен выполнится в любом случае
				ed.putInt(C.CONST_SALARI+i, array_salari[count]);
				ed.putInt(C.CONST_ADVANCE+i, array_advance[count]);
				ed.putString(C.CONST_DATE+i, array_date[count]);
				//Преобразуем если неообходимо Время для сортировки
				if (array_date_time[count] == -1){
					array_date_time[count] = parseDate(array_date[count]);
				}
				ed.putInt(C.CONST_TIME+i, array_date_time[count]);
				count++;
			}
		}	
		ed.commit();
		Log.d(C.LOG, "saved "+length+" rows");
	}
	
	//Метод считывает всю таблицу и заполняет соответствующие массивы.
	void load(){
		spMemory = getPreferences(MODE_PRIVATE);	
		length = spMemory.getInt(CONST_LENGTH, 999);
		
		//проверка первого запуска	
		if (length == 999){
			Log.d("myLog", "create sample list position");
			array_salari[0] = 10000;
			array_advance[0] = 5000;
			array_date[0] = "Образец";
			length = 1;
			savePosition(length);
		}
		
		for (int i = 0; i < length ; i++){
			array_salari[i] = spMemory.getInt(CONST_SALARI+(i+1), 0);
			array_advance[i] = spMemory.getInt(CONST_ADVANCE+(i+1), 0);
			array_date[i] = spMemory.getString(CONST_DATE+(i+1), "--/--");
			array_date_time[i] = spMemory.getInt(CONST_TIME+(i+1), -1);
			Log.d(C.LOG, "load record #"+(i+1)+": s"+array_salari[i]
					+" a"+array_advance[i]+" d"+array_date[i]
						+", t"+array_date_time[i]);
		}
		
		Log.d(C.LOG,"load finish");
	}
	
	//извлекаем из Даты числовые значения месяца и года
	int parseDate(String dateString){
		int date;
		
		int month = 0;
		int year = 0;
		String tempNumb = new String();
		
		//разбираем дату на числа - месяц и год
		for(int i = 0; i < dateString.length(); i++){
			if (Character.isDigit(dateString.charAt(i))){
				tempNumb += dateString.charAt(i);
			}else if(!tempNumb.equalsIgnoreCase("") && month == 0){//проверяем что слово не пустое
				//Log.d(C.LOG, "numb = "+tempNumb);
				month = Integer.parseInt(tempNumb);
				tempNumb ="";
			}else if(!tempNumb.equalsIgnoreCase("") && month != 0){
				year = Integer.parseInt(tempNumb);
			}
			
			if (i == (dateString.length()-1) && !tempNumb.equalsIgnoreCase("") && year == 0){
				//Log.d(C.LOG, "numb = "+tempNumb);
				year = Integer.parseInt(tempNumb);
			}
		}
		
		if (year > 2000){//Проверяем вид записи года, и вычисляем дату
			date = month + (year-2000)*12;
		}else{
			date = month + year*12;
		}
		
		Log.d(C.LOG, "parse String Date: "+dateString+", Time = "+date+", m="+month+", y="+year);
		
		return date;
	}
	
	
	@Deprecated //deprecated , устаревший метод, больше не используется
	//Метод меняет размер таблицы, убирает последнюю строку, при этом данные остаются.
	void deleteLastPosition(){	
		if (length == 0) return;
		Log.d("myLog", "length = 0, can't delete");
		
		length--;
		spMemory = getPreferences(MODE_PRIVATE);
		Editor ed = spMemory.edit();
		ed.putInt(C.CONST_LENGTH, length);
		ed.commit();	
	}
	
	void deletePosition(int delPosition){
		if (length == 0 || delPosition < 0 || delPosition > 50) {
			Toast.makeText(this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
			return;//завершение метода
		}
		//Log.d(C.LOG, "-Удаление пункта № " + delPosition);
		
		length--;
		array_salari[delPosition] = -1;
		save();	
		load();
	}
	
	void sortList(){
		int[] position = new int[length];
		
		for (int x = 0; x < length; x++){
			for(int y = 0; y < length; y++){
				if (array_date_time[x] > array_date_time[y]){
					position[x]++;		
				}else if (array_date_time[x] == array_date_time[y] && x < y){
					position[x]++;
				}
			}
		}
		
		int[] tmpSalari = array_salari.clone();
		int[] tmpAdvance = array_advance.clone();
		int[] tmpTime = array_date_time.clone();
		String[] tmpDate = array_date.clone();
		
		for (int i = 0; i < length; i++){
			//int x = position[i];
			//Log.d(C.LOG, "sort new #"+(x+1)+", t"+array_date_time[x]+" <== old #"+(i+1) +", t"+tmpTime[i]);
			array_salari[position[i]] = tmpSalari[i];
			array_advance[position[i]] = tmpAdvance[i];
			array_date_time[position[i]] = tmpTime[i];
			array_date[position[i]] = tmpDate[i];		
		}
	}
	
	//Обнавление заголовка. Заголовок заполняется по строке positionLine
	void updateHead(int positionLine){
		if (positionLine < 0 | positionLine > 50) return;

  	  	//Меняем текст в TextView согласно нажатому пункту
  	  	text_pay.setText("Зарплата: "+array_salari[positionLine]);
  	  	text_advance.setText("Аванс: "+array_advance[positionLine]);  	
  	  	text_date.setText(array_date[positionLine]);
  	  
  	  	//расчет и вывод остальных параметров
  	  	all_pay = array_salari[positionLine]+array_advance[positionLine];
  	  	text_all_pay.setText("Общее: "+all_pay);
  	  
  	  	int value = all_pay*10/100;
  	  	text_moneybox.setText("Копилка 10%: "+value);
  	  	value = all_pay*20/100;
  	  	text_bank.setText("Банк 20%: "+value);
  	  	value = 3000;
  	  	text_rent.setText("Квартплата: "+value);
  	  	value = all_pay*30/100;
  	  	text_purse.setText("Кошелек 30%: "+value);
  	  	value = all_pay-(all_pay*20/100+all_pay*30/100+all_pay*10/100+3000);
  	  	text_rest.setText("Остаток: "+value);
	}
}
