package ru.razoom.develop.budget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddActivity extends Activity implements BudgetConstants {
		
	Button bAccept;
	EditText edit_pay, edit_advance, edit_date;
	
	final Context ctx = this; //ссылка для запуска Toast
	
	//Запускается при создании приложения
	protected void onCreate(Bundle s){
		super.onCreate(s);
		setContentView(R.layout.activity_add);
				
		//Находим ссылки на вью обьекты
		bAccept = (Button) findViewById(R.id.bAccept);
		edit_pay = (EditText) findViewById(R.id.edit_pay);
		edit_advance = (EditText) findViewById(R.id.edit_advance);
		edit_date = (EditText) findViewById(R.id.edit_date);
		
		//получаем интент вызвавший активити
		Intent intent = getIntent();
		//берем данные из интента
		int mode = intent.getIntExtra("mode", 1);
						
		//Если редактирование, то заполняем поля значениями
		if(mode == POS_EDIT){
			edit_pay.setText(""+intent.getIntExtra(CONST_SALARI, 0));
			edit_advance.setText(""+intent.getIntExtra(CONST_ADVANCE, 0));
			edit_date.setText(intent.getStringExtra(CONST_DATE));
		}else if (mode == C.POS_ADD){
			/*
			//Автозаполнение даты
			Date today = new Date();
			SimpleDateFormat formatDate = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
			SimpleDateFormat formatDay = new SimpleDateFormat("dd", Locale.getDefault());
			String date = formatDate.format(today);
			edit_date.setText(date);
			
			int month = Integer.valueOf(formatDay.format(today));
			*/
		}
				
		
		
		//обрабатываем нажатие на кнопку
		bAccept.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				
				//Ставим исключения т.к. parseInt выдаст ошибку, если поле ввода пустое
				try{				
					Intent intent = new Intent();
					intent.putExtra(CONST_DATE, edit_date.getText().toString());					
					intent.putExtra(CONST_SALARI, Integer.parseInt(edit_pay.getText().toString()));
					intent.putExtra(CONST_ADVANCE, Integer.parseInt(edit_advance.getText().toString()));
					setResult(RESULT_OK, intent);			
				}catch(Exception e){				
					Toast.makeText(ctx, "Ошибка ввода", Toast.LENGTH_SHORT).show();
					Log.d("myLog","error = "+e.toString());				
				}finally{
					finish();
				}
			}
		});	
	}
}
