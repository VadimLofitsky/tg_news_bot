from openjdk:11

run apt update && apt install ffmpeg -y

run mkdir /app
run chmod -R ugo+rwx /app
run mkdir /app/screens
run chmod -R ugo+rwx /app/screens
run mkdir /app/log
run chmod -R ugo+rwx /app/log
copy tg_news_bot-0.0.1.jar /app/app.jar
copy docker.yml /app/app.yml
copy tg_bot.yml /app/tg_bot.yml

expose 80
expose 4321

workdir /app

cmd java -jar -DappConfigPath=./app.yml -DbotConfigPath=./tg_bot.yml app.jar
