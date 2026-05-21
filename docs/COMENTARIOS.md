# 💬 Comentários da Equipe

Reflexões pessoais de cada membro sobre o desenvolvimento do projeto — decisões tomadas, dificuldades encontradas e o que cada um aprendeu no processo.

---

## Eduardo Santos Cruz

> *(em breve)*

---

## Gabriel Conceição da Silva

**Sobre o Dashboard**

> Um dos pontos em que o Claude Code foi usado foi na criação do dashboard, por ser algo adicional feito em JavaFX — uma tecnologia que eu ainda estava estudando à época. De qualquer forma, foi uma boa experiência poder fuçar o código completo depois e editar por conta própria parâmetros que a IA havia deixado de fora.

**Sobre os comentários no código**

> Outro uso foi para adicionar comentários. Sempre que eu terminava de fazer uma edição, exclusão ou adição no projeto, os comentários eram revisados com suporte externo. Por isso, alguns não seguem o mesmo padrão do restante do projeto.

**Sobre segurança**

> Depois de muito pensar, e para não complicar o projeto, foi decidido que o mais sábio seria deixar a segurança da folha de pagamento com a empresa e com a máquina que executa o sistema. Armazenar senha localmente, por exemplo, seria um tiro no pé. Há travas verbais em alguns pontos, mas eu sei melhor que ninguém que, em um produto real, isso não seria suficiente. Partes como o menu ADM deveriam ser fechadas com senha — atualmente são protegidas apenas pela lógica de seleção de perfil no início da sessão.

**Sobre os perfis de acesso**

> A ideia de perfis esteve na minha cabeça desde cedo, esperando o momento certo para entrar no sistema. O menu ADM acabou crescendo para 9 opções administrativas, pensadas para o sistema durar mais tempo. A ideia original dos perfis era mais complexa: além da escolha no início da sessão, haveria uma troca de perfil dentro do menu, sem precisar sair, e uma opção dentro das configurações para definir quais opções do menu ADM poderiam aparecer para o funcionário comum — o dashboard analítico seria o maior exemplo disso, já que a necessidade varia de empresa para empresa. Essas duas funcionalidades foram removidas e foi mantida apenas a escolha de perfil por sessão. É o que mais combina com o escopo do projeto, entrega uma versão para o usuário comum que se alinha à ideia original pedida, e ainda abre espaço para crescimento. Acredito que foi uma boa adição.

**Sobre dependências externas**

> Com muito esforço, não foram usadas APIs externas — apenas as bibliotecas nativas do Java. Essa foi uma escolha mais pessoal, para manter o projeto "puritano".

**Sobre o `ConsoleUI.java`**

> Pessoalmente, o `ConsoleUI.java` foi o arquivo mais difícil de fazer. Não havia um molde gerado por IA como base, como aconteceu com o dashboard — tudo foi feito na mão. E o terminal tem suas limitações: não é tão responsivo quanto uma interface JavaFX. Centralizar títulos, evitar que frases fugissem do padrão visual, fazer os menus parecerem coesos — tudo isso deu bastante trabalho durante o processo.

---

## Pedro Alonso Martins Fernandes

> *(em breve)*
