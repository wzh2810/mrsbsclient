package com.wz.mrsbsclient;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.image.SmartImageView;
import com.wz.mrsbsclient.bean.News;

public class MainActivity extends Activity {
	
	List<News> newsList;

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			ListView lv = (ListView) findViewById(R.id.lv);
			lv.setAdapter(new MyAdapter());
		};
	};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getNewsInfo();
    }
    
    class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return newsList.size();
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder mHolder = null;
			if(convertView == null) {
				convertView = View.inflate(MainActivity.this, R.layout.item_listview, null);
				
				//����viewHolder��װ������Ŀʹ�õ���� 
				mHolder = new ViewHolder();
				mHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
				mHolder.tv_detail = (TextView) convertView.findViewById(R.id.tv_detail);
				mHolder.tv_comment = (TextView) convertView.findViewById(R.id.tv_comment);
				mHolder.siv = (SmartImageView) convertView.findViewById(R.id.siv);
				
				//��viewHolder��װ��view��������view����ʱ��viewHolderҲ������
				convertView.setTag(mHolder);
			} else {
				//��view��ȡ�������viewHolder��viewHolder�о������е�������󣬲���Ҫ��ȥfindViewById
				mHolder = (ViewHolder) convertView.getTag();
			}
			//����Ŀ�е�ÿ���������Ҫ��ʾ������
			News news = newsList.get(position);
			mHolder.tv_title.setText(news.getTitle());
			mHolder.tv_detail.setText(news.getDetail());
			mHolder.tv_comment.setText(news.getComment() + "������");
		
			mHolder.siv.setImageUrl(news.getImageUrl());
			Log.i("TAG",news.getImageUrl());
			return convertView;
		}
    	//����Ŀ��Ҫʹ�õ������������װ���������
		class ViewHolder {
			TextView tv_title;
			TextView tv_detail;
			TextView tv_comment;
			SmartImageView siv;
		}

		@Override
		public Object getItem(int position) {
			
			return newsList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		
    }

	private void getNewsInfo() {
		Thread t = new Thread(){
			@Override
			public void run() {
				String path = "http://192.168.3.17:8080/news.xml";
				try {
					URL url = new URL(path);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setReadTimeout(8000);
					conn.setConnectTimeout(8000);
					
					if(conn.getResponseCode() == 200) {
						//�������Ϣ��һ��xml�ļ����ı���Ϣ����xml������ȥ����������Ҫ��Ϊ�ı�ȥ����
						InputStream is = conn.getInputStream();
						getNewsFromStream(is);
					}
				}catch(Exception e) {
					e.printStackTrace();
				}
			}

			
		};
		t.start();
	}
	
	private void getNewsFromStream(InputStream is) {
		XmlPullParser xp = Xml.newPullParser();
		try {
			xp.setInput(is, "utf-8");
			//��ȡ�¼����ͣ�ͨ���¼������жϳ���ǰ�������Ǻ�ʲô�ڵ�
			int type = xp.getEventType();
			News news = null;
			while(type != XmlPullParser.END_DOCUMENT) {
				switch(type) {
				case XmlPullParser.START_TAG:
					if("newslist".equals(xp.getName())) {
						newsList = new ArrayList<News>();
					} else if("news".equals(xp.getName())) {
						news = new News();
					} else if("title".equals(xp.getName())) {
						String title = xp.nextText();
						news.setTitle(title);
					} else if("detail".equals(xp.getName())) {
						String detail = xp.nextText();
						news.setDetail(detail);
					} else if("comment".equals(xp.getName())) {
						String comment = xp.nextText();
						news.setComment(comment);
					} else if("image".equals(xp.getName())) {
						String image = xp.nextText();
						news.setImageUrl(image);
					}
					break;
				case XmlPullParser.END_TAG:
					if("news".equals(xp.getName())) {
						newsList.add(news);
					}
					break;
				}
				//ָ���ƶ�����һ���ڵ㲢�����¼�����
				type = xp.next();
			}
			for(News n: newsList) {
				Log.i("TAG", n.toString());
			}
			handler.sendEmptyMessage(1);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

 
    
}
