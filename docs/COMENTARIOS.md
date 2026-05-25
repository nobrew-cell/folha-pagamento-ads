 # 💬 Comentários da Equipe

Reflexões pessoais de cada membro sobre o desenvolvimento do projeto — decisões tomadas, dificuldades encontradas e o que cada um aprendeu no processo.

---

## Eduardo Santos Cruz

> *(em breve)*

---

## Gabriel Conceição da Silva

**Sobre a estrutura dos `.java`**

> Nas primeiras tentativas do projeto, eu pessoalmente fiz um único `.java`. Mas logo percebi o quão sem sentido isso seria a longo prazo em futuras refatorações. Ter que ficar pesquisando onde eu coloquei tal função não era nada produtivo. Pois quando desse pal em alguma coisa, corrigir seria impossível ou pelo menos bem dificultado. Então como Napoleão uma vez disse, *"Dividir para Conquistar"*. Dividi bem cada função do sistema em seu `.java` respectivo, para que esse problema não se repetisse. Essa foi uma *boa prática* usando a funcionalidade de pacotes que existe no próprio Java.

**Sobre o Dashboard**

> Um dos pontos em que o Claude Code foi usado foi na criação do dashboard, por ser algo adicional feito em JavaFX — uma tecnologia que eu ainda estava estudando à época. De qualquer forma, foi uma boa experiência poder fuçar o código completo depois e editar por conta própria parâmetros que a IA havia deixado de fora.

**Sobre os comentários no código**

> Outro uso foi para adicionar comentários. Sempre que eu terminava de fazer uma edição, exclusão ou adição no projeto, os comentários eram revisados com suporte externo. Por isso, alguns não seguem o mesmo padrão do restante do projeto.

**Sobre os releases e versionamento do projeto**

> Essa foi uma partes mais complicadas de se manter e mais chatinha de seguir. Como as mudanças estavam no meu controle e não do git, não tem uma real consistência entre os releases, por exemplo, e o versionamento. Se bem perceber, verá que por exemplo o projeto não começa com v1.0, pois essa versão é a minha local. Isso se repete nas outras versões posteriores. É um padrão pessoal, que só existe pois os meus testes pessoais locais, eu não desejaria de forma alguma que aparecesse no github ou fosse invadido pelo git. Se tivesse uma v0.0, ela seria o primeiro .java do projeto. Ele nem aparece no github, pois faz parte de testes pessoais iniciais. A maioria dos commits, ou são novas funções que eu coloquei, ou são coisas que deixei passar entre uma IDE e outra. Se ficar curioso, esse foi o esquema atual dos releases do projeto:

---

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/refs/heads/evolution/sistemaFolha-final/assets/rg_releases_black.svg">
  <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/evolution/sistemaFolha-final/assets/rg_releases_white.svg">
  <img src="https://raw.githubusercontent.com/nobrew-cell/folha-pagamento-ads/evolution/sistemaFolha-final/assets/rg_releases_black.svg" alt="Diagrama de Releases do Projeto" style="max-width: 100%;">
</picture>

<p align="center">
  <sub><i>Diagrama de planejamento das releases do projeto, divididas entre o escopo <b>Local</b> (desenvolvimentos e testes que podem ser retidos ou unificados) e o escopo <b>GitHub</b> (entregas oficiais e versionadas via Git).</i></sub>
</p>

---

**Sobre as IDEs**

> Eu pessoalmente usei mais de uma IDE no projeto, eu fazia para treino pessoal e aprendizagem mesmo no NetBeans Apache e passava para o VS Code que tenho mais facilidade de configurar coisas como o Git e o Github. Falando da parte do NetBeans. Minha experiência com ele até que foi de boa. Realmente, ele demora em algumas coisas que o Vs Code ganha em desepenho. Mas provavelmente deve ser por causa do meu computador.  

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
