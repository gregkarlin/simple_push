import mysql.connector

cnx = mysql.connector.connect(user='root', database='new_subscriptions', password='lister')
cursor = cnx.cursor()
