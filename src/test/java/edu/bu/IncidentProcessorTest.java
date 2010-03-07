package edu.bu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class IncidentProcessorTest {
	private static final String exampleXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><statuses type=\"array\"><status>  <created_at>Thu Dec 17 15:44:28 +0000 2009</created_at>  <id>6766686906</id>  <text>#firebug console &amp; script tabs disable the js JIT! http://antennasoft.net/robcee/2009/12/15/firebug-and-the-jit/</text>  <source>web</source>  <truncated>false</truncated>  <in_reply_to_status_id></in_reply_to_status_id>  <in_reply_to_user_id></in_reply_to_user_id>  <favorited>false</favorited>  <in_reply_to_screen_name></in_reply_to_screen_name>  <user>    <id>15517060</id>    <name>David LaPalomento</name>    <screen_name>dlapalomento</screen_name>    <location>Boston, MA</location>    <description></description>    <profile_image_url>http://a1.twimg.com/profile_images/146026342/dml-2009-05-73x73_normal.jpg</profile_image_url>    <url></url>    <protected>false</protected>    <followers_count>43</followers_count>    <profile_background_color>9ae4e8</profile_background_color>    <profile_text_color>000000</profile_text_color>    <profile_link_color>0000ff</profile_link_color>    <profile_sidebar_fill_color>e0ff92</profile_sidebar_fill_color>    <profile_sidebar_border_color>87bc44</profile_sidebar_border_color>    <friends_count>38</friends_count>    <created_at>Mon Jul 21 16:26:47 +0000 2008</created_at>    <favourites_count>0</favourites_count>    <utc_offset>-18000</utc_offset>    <time_zone>Eastern Time (US &amp; Canada)</time_zone>    <profile_background_image_url>http://s.twimg.com/a/1266605807/images/themes/theme1/bg.png</profile_background_image_url>    <profile_background_tile>false</profile_background_tile>    <notifications></notifications>    <geo_enabled>false</geo_enabled>    <verified>false</verified>    <following></following>    <statuses_count>39</statuses_count>    <lang>en</lang>    <contributors_enabled>false</contributors_enabled>  </user>  <geo/>  <contributors/></status><status>  <created_at>Sun Nov 15 06:55:04 +0000 2009</created_at>  <id>5730872530</id>  <text>interesting #rant on the state of #programming languages today.  #firebug bit over the top, but worth a read: http://bit.ly/3UXCO6</text>  <source>web</source>  <truncated>false</truncated>  <in_reply_to_status_id></in_reply_to_status_id>  <in_reply_to_user_id></in_reply_to_user_id>  <favorited>false</favorited>  <in_reply_to_screen_name></in_reply_to_screen_name>  <user>    <id>15517060</id>    <name>David LaPalomento</name>    <screen_name>dlapalomento</screen_name>    <location>Boston, MA</location>    <description></description>    <profile_image_url>http://a1.twimg.com/profile_images/146026342/dml-2009-05-73x73_normal.jpg</profile_image_url>    <url></url>    <protected>false</protected>    <followers_count>43</followers_count>    <profile_background_color>9ae4e8</profile_background_color>    <profile_text_color>000000</profile_text_color>    <profile_link_color>0000ff</profile_link_color>    <profile_sidebar_fill_color>e0ff92</profile_sidebar_fill_color>    <profile_sidebar_border_color>87bc44</profile_sidebar_border_color>    <friends_count>38</friends_count>    <created_at>Mon Jul 21 16:26:47 +0000 2008</created_at>    <favourites_count>0</favourites_count>    <utc_offset>-18000</utc_offset>    <time_zone>Eastern Time (US &amp; Canada)</time_zone>    <profile_background_image_url>http://s.twimg.com/a/1266605807/images/themes/theme1/bg.png</profile_background_image_url>    <profile_background_tile>false</profile_background_tile>    <notifications></notifications>    <geo_enabled>false</geo_enabled>    <verified>false</verified>    <following></following>    <statuses_count>39</statuses_count>    <lang>en</lang>    <contributors_enabled>false</contributors_enabled>  </user>  <geo/>  <contributors/></status></statuses>";
	
	@Test
	public void handle() throws Exception {
		XMLReader reader = XMLReaderFactory.createXMLReader();
		StringOutputStreamFactory outputFactory = new StringOutputStreamFactory();
		reader.setContentHandler(new IncidentHandler(outputFactory));
		reader.parse(new InputSource(new StringReader(exampleXml)));
		
		assertEquals(3, outputFactory.outputs.size());
		assertTrue(outputFactory.outputs.containsKey("#firebug"));
		assertTrue(outputFactory.outputs.containsKey("#rant"));
		assertTrue(outputFactory.outputs.containsKey("#programming"));
		Document firebug = DocumentHelper.parseText(outputFactory.outputs.get(
				"#firebug").asString());
		assertEquals(2, firebug.getRootElement().elements().size());
		Document rant = DocumentHelper.parseText(outputFactory.outputs.get(
				"#rant").asString());
		assertEquals(1, rant.getRootElement().elements().size());
		Document programming = DocumentHelper.parseText(outputFactory.outputs
				.get("#programming").asString());
		assertEquals(1, programming.getRootElement().elements().size());
	}


}
