# 💬 Comentários da Equipe

Reflexões pessoais de cada membro sobre o desenvolvimento do projeto — decisões tomadas, dificuldades encontradas e o que cada um aprendeu no processo.


**Legenda de Identidade Visual:**
>Para facilitar a navegação, cada membro possui uma cor distinta:

> [!NOTE]
> - 🟦 Azul: Eduardo Santos (Perspectiva de Experiência do Usuário)

>[!TIP]
> - 🟩 Verde: Gabriel Silva (Perspectiva de Desenvolvimento e Arquitetura)

> [!WARNING]
> - 🟨 Amarelo: Pedro Fernandes (Perspectiva de [Área do Pedro])

> &nbsp;

---

## 🟦 Eduardo Santos Cruz

**Sobre a utilidade do Perfil Funcionário**
> [!NOTE]
> A área do funcionário é muito útil dentro do sistema, pois permite que o colaborador consulte suas próprias informações de pagamento de forma mais prática. Por meio dela, o funcionário pode acessar dados como salário, descontos, benefícios e valores relacionados à folha. Isso evita que ele precise procurar o RH ou a administração sempre que tiver uma dúvida simples.

**Sobre a eficiência e organização**
> [!NOTE]
> A organização das informações também ajuda bastante na compreensão dos dados apresentados. Quando tudo está reunido em um só lugar, a consulta se torna mais rápida e eficiente. Além disso, essa funcionalidade contribui para uma rotina mais independente. No geral, essa parte do sistema facilita o acesso às informações pessoais do funcionário.

**Sobre a autonomia e experiência do usuário**
> [!NOTE]
> A experiência do usuário funcionário pode ser considerada positiva, principalmente pela autonomia que o sistema oferece. O colaborador consegue visualizar informações importantes sem precisar passar por processos manuais demorados. Isso torna o uso do sistema mais prático e melhora a relação entre funcionário e empresa.

**Sobre transparência e segurança financeira**
> [!NOTE]
> Outro ponto importante é a transparência, já que o usuário consegue acompanhar melhor os valores da folha de pagamento. Com essas informações disponíveis, fica mais fácil entender o valor recebido no final do mês. O sistema também transmite mais segurança, pois permite que o funcionário acompanhe seus próprios dados financeiros. Dessa forma, a experiência se torna mais clara, acessível e confiável.

**Sobre o acompanhamento detalhado da folha - Opção `[4]` Gerar Folha de Pagamento**
> [!NOTE]
> Durante o uso da área do funcionário, uma experiência interessante é poder conferir os detalhes da folha com calma. O sistema permite que o colaborador observe melhor os valores lançados e compare as informações apresentadas. Isso ajuda a evitar dúvidas no momento de receber o pagamento, pois os dados ficam disponíveis para consulta.

**Sobre o controle e conforto do colaborador**
> [!NOTE]
> A sensação é de que o funcionário tem mais controle para acompanhar sua própria situação financeira. Também é uma experiência mais confortável, já que a consulta pode ser feita no próprio sistema. Esse contato direto com as informações torna o processo menos confuso para o usuário. No geral, a área do funcionário passa uma impressão de organização e facilita o acompanhamento da folha de pagamento.

---

## 🟩 Gabriel Conceição da Silva

**Sobre o `Escopo Atual` vs `Escopo da UC Dual`**
>[!TIP]
> Se esse projeto quisesse sobreviver e ficar no meu github, eu teria que inevitavelmente adicionar algumas leves alterações ao escopo geral pedido pela UC Dual. Parte disso foi por causa da minha empolgação, mas prometo que o resto foi em meus momentos mais lúcidos. Um exemplo é o salário base que é fixo, porém nesse sistema é editável via menu adm. O que não é o *perfeitamente fixo* citado no projeto.
> 
> Mas isso foi colocado principalmente por facilidade pessoal ao administrador, para ele não ter que ir trocar manualmente no código do projeto. E sendo sincero, tem muita pouca diferença, pois mesmo um salário fixo, pode ser alterado no código. Então mesmo se eu colocasse um:

```java
public static final double SALARIO_BASE = 2000.00;
```
>[!TIP]
> Ainda assim, não seria perfeitamente fixo. Poderia até ser fixo no sistema, mas tenho minhas dúvidas se seria realmente fixo em geral. Com certeza não seria imutável, apenas menos configurável dentro do sistema. Então como boa prática, e querendo ser "amigo" do administrador, eu coloquei ele como editável no menu adm, até porque salários mudam.
>
> Além disso, ele casa muito bem com as outras opções do menu adm, como o novo mês, onde por exemplo, o mês anterior poderia ser `2000`, mas o mês atual poderia ser `2500` após reajustes. Acredito que o escopo pedido inicialmente no projeto é muito bom, mas pessoalmente quis que esse projeto durasse mais de um mês na empresa e no meu repositório.
>
> E sendo mais sincero ainda, esse sistema serve mais que perfeitamente para a nossa empresa hipotética e também para outras, se assim desejássemos.

**Sobre os testes**
>[!TIP]
> Alguns eu fiz pessoalmente, mas a maioria ou era de lógica ao depurar o código ou era algum erro que deixei passar no `ConsoleUI.java`. Mas alguns erros eu só percebi ao conversar com os membros do projeto. Muitas coisas ficavam fora da minha visão por eu estar olhando demais o cenário geral e pela adrenalina de fazer o projeto a tempo. Então as análises e ideias dos meus colegas, foram a melhor ajuda possível. Muitas vezes nem eram por intenção, mas cada conversa me deu novas ideias e perpectiva diferente de um único projeto. E alguns de seus comentários podem ser vistos nesse documento aqui mesmo. Então aproveitem.

**Sobre a estrutura dos `.java`**
>[!TIP]
> Nas primeiras tentativas do projeto, eu pessoalmente fiz um único `.java`. Mas logo percebi o quão sem sentido isso seria a longo prazo em futuras refatorações. Ter que ficar pesquisando onde eu coloquei tal função não era nada produtivo. Pois quando desse pau em alguma coisa, corrigir seria impossível ou pelo menos bem dificultado. Então como Napoleão uma vez disse, *"Dividir para Conquistar"*. Dividi bem cada função do sistema em seu `.java` respectivo, para que esse problema não se repetisse. Essa foi uma *boa prática* usando a funcionalidade de pacotes que existe no próprio Java.

**Sobre o Dashboard**
>[!TIP]
> Um dos pontos em que o Claude Code foi usado foi na criação do dashboard, por ser algo adicional feito em Swing — uma tecnologia que eu ainda estava estudando à época. De qualquer forma, foi uma boa experiência poder fuçar o código completo depois e editar por conta própria parâmetros que a IA havia deixado de fora.

**Sobre os comentários no código**
>[!TIP]
> Outro uso foi para adicionar comentários. Sempre que eu terminava de fazer uma edição, exclusão ou adição no projeto, os comentários eram revisados com suporte externo. Por isso, alguns não seguem o mesmo padrão do restante do projeto.

**Sobre os releases, versionamento e commits do projeto**
>[!TIP]
> Essa foi uma das partes mais complicadas de se manter e para os releases, a mais chatinha de seguir no projeto. Como as mudanças estavam no meu controle e não totalmente centralizadas no Git, não teve uma real *consistência* entre os releases, por exemplo, e o versionamento e commits.
>
> Se bem perceber, verá que por exemplo o projeto não começa com `v1.0`, pois essa versão é a minha local. Isso se repete nas outras versões posteriores. É um padrão pessoal, que só existe pois os meus testes pessoais locais, eu não desejaria de forma alguma que aparecesse no github ou fossem invadidos pelo Git/Github. Se tivesse uma `v0.0`, ela seria o primeiro `.java` do projeto. Ele nem aparece no Github, pois faz parte de testes pessoais iniciais.
>
> A maioria dos commits, ou são novas funções que eu coloquei, ou são coisas que deixei passar entre uma IDE e outra. Se ficar curioso, dê uma olhada no diagrama de releases: [`docs/DIAGRAMAS.md`](./docs/DIAGRAMAS.md)

**Sobre as IDEs**
>[!TIP]
> Eu pessoalmente usei mais de uma IDE no projeto. Eu fazia para treino pessoal e aprendizagem mesmo no NetBeans Apache e passava para o VS Code aquilo que tinha mais facilidade de configurar, como o Git e o Github. Falando da parte do NetBeans. Minha experiência com ele até que foi de boa. Realmente, ele demora em algumas coisas que o VS Code ganha em desempenho. Mas provavelmente deve ser por causa do meu computador. Mas a interface dele é agradável, tem suporte bom ao Swing para a criação de interface gráfica, que usei no dashboard, e até me cansei olhando para a interface do console ui no terminal.

**Sobre segurança**
>[!TIP]
> Depois de muito pensar, e para não complicar o projeto, foi decidido que o mais sábio seria deixar a segurança da folha de pagamento com a empresa e com a máquina que executa o sistema. Armazenar senha localmente, por exemplo, seria um tiro no pé. Há travas verbais em alguns pontos, mas eu sei melhor que ninguém que, em um produto real, isso não seria suficiente. Partes como o menu ADM deveriam ser fechadas com senha — atualmente são protegidas apenas pela lógica de seleção de perfil no início da sessão.

**Sobre persistência de dados**
>[!TIP]
> No início, os cadastros eram persistidos diretamente nas variáveis vulgo memória RAM usando o próprio `ArrayList`. Funcionava? Sim, mas para a função de exportar fazer sentido que havia sido colocada, pensei que armazená-lo em algum arquivo que o Excel e outros visualizadores de planilhas aceitam seria o mais sábio.
>
> Minha primeira ideia foi o próprio `.xlsx` do Excel, porém para exportar nele eu teria que usar uma API ou biblioteca externa, então eu acabei abandonando a ideia.
>
> A segunda ideia então foi na verdade usar o CSV, que na minha cabeça, funcionaria melhor. Ele era lido facilmente pelo Excel, e por ser um arquivo de "texto", dava para eu lê-lo direto na IDE, o que só havia preenchido minha satisfação, porém ao fazer o hard test, percebi que para valores monetários brasileiros, que usa a `,` para separar os centavos, ele acaba não sendo útil e pode até corromper os dados, pois o csv usa como separador a `,` nos EUA e `;` no Brasil. Eu até tentei usar e fazer o sistema gerar o CSV com separações `;`, e fazer o Excel lê-lo, mas ele ainda tinha o perigo da pessoa digitar errado e corromper uma coluna inteira de cadastros.
>
> Então, uma alternativa que apareceu foi TSV, em vez de `,` ou `;` que o usuário pode digitar por acidente, o TSV usa tabulação para as separações. Além dele, também tem o XLS, só que esse só vem da exportação, que é apenas um agrado para o usuário, seja funcionário ou administrador, que possui coloração para ficar mais fácil de ler.
>
> Existem outras extensões de arquivo no projeto que guardam dados, só que acredito que elas são melhor explicadas no .html da arquitetura do projeto. Só acessar aqui e baixá-lo: [`docs/ARQUITETURA.html`](./docs/ARQUITETURA.html)

**Sobre os perfis de acesso**
>[!TIP]
> A ideia de perfis esteve na minha cabeça desde cedo, esperando o momento certo para entrar no sistema. O menu ADM acabou crescendo para `9` opções administrativas, pensadas para o sistema durar mais tempo. A ideia original dos perfis era mais complexa: além da escolha no início da sessão, haveria uma troca de perfil dentro do menu, sem precisar sair, e uma opção dentro das configurações para definir quais opções do menu ADM poderiam aparecer para o funcionário comum — o dashboard analítico seria o maior exemplo disso, já que a necessidade varia de empresa para empresa. Essas duas funcionalidades foram removidas e foi mantida apenas a escolha de perfil por sessão. É o que mais combina com o escopo do projeto, entrega uma versão para o usuário comum que se alinha à ideia original pedida, e ainda abre espaço para crescimento. Acredito que foi uma boa adição.

**Sobre dependências externas**
>[!TIP]
> Com muito esforço, não foram usadas APIs externas — apenas as bibliotecas nativas do Java. Essa foi uma escolha mais pessoal, para manter o projeto "puritano".

**Sobre o `ConsoleUI.java`**
>[!TIP]
> Pessoalmente, o `ConsoleUI.java` foi o arquivo mais difícil de fazer. Não havia um molde gerado por IA como base, como aconteceu com o dashboard — tudo foi feito na mão. E o terminal tem suas limitações: não é tão responsivo quanto uma interface JavaFX ou Swing. Centralizar títulos, evitar que frases fugissem do padrão visual, fazer os menus parecerem coesos — tudo isso deu bastante trabalho durante o processo.

**Sobre o aprendizado**
>[!TIP]
> Em resumo, um dos maiores desafios do projeto foi equilibrar o escopo acadêmico com a vontade de transformá-lo em algo mais próximo de um sistema real. Em vários momentos, confesso que foi necessário decidir entre *“fazer exatamente o mínimo pedido”* ou implementar estruturas que tornassem o sistema mais sustentável, organizado e reutilizável no futuro. Tentei manter o núcleo principal fiel aos conceitos da UC, enquanto adicionava funcionalidades extras sem comprometer a simplicidade geral do projeto. Mas não sei se alcancei completamente o que a UC DUAL exigiu, mas tenho certeza que esse sistema me ensinou bastante coisa.

---

## 🟨 Pedro Alonso Martins Fernandes
> [!WARNING]
> *(em breve)*
