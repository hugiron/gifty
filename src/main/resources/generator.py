gift_query = "insert into \"Gifts\" (name, url) values ('%s','%s');\n"
question_query = "insert into \"Questions\" (question) values ('%s');\n"

if __name__ == '__main__':
	gifts = [line.strip().split(',') for line in open('gifts.csv', 'r') if line.strip()][1:]
	questions = [line.strip() for line in open('questions.csv', 'r') if line.strip()][1:]
	with open('commands.sql', 'w') as file:
		for gift in gifts:
			file.write(gift_query % (gift[0], gift[3]))
		for question in questions:
			file.write(question_query % question)
