package com.piedpiper.components

import com.piedpiper.server.directives.AuthDirective
import com.piedpiper.server.handlers.{CandidatesListHandler, CreateReferralLinkHandler, LoginHandler, QuestionnaireHandler, ResourceHandler, UserInfoHandler}

import scala.concurrent.Future

class HandlerComponent(val loginHandler: LoginHandler,
                       val authDirective: AuthDirective,
                       val userInfoHandler: UserInfoHandler,
                       val createReferralLinkHandler: CreateReferralLinkHandler,
                       val resourceHandler: ResourceHandler,
                       val questionnaireHandler: QuestionnaireHandler,
                       val candidatesListHandler: CandidatesListHandler)

object HandlerComponent {
  def mk(baseComponent: BaseComponent,
         daoComponent: DaoComponent,
         configComponent: ConfigComponent,
         serviceComponent: ServiceComponent): Future[HandlerComponent] = {
    import baseComponent._

    val loginHandler = new LoginHandler(daoComponent.userDao, daoComponent.userSessionDao)
    val authDirective = new AuthDirective(daoComponent.userDao, daoComponent.userSessionDao)
    val userInfoHandler = new UserInfoHandler(authDirective)
    val questionnaireHandler = new QuestionnaireHandler(
      questionnaireDao = daoComponent.questionnaireDao,
      emailService = serviceComponent.emailService,
      candidateDao = daoComponent.candidateDao
    )
    val createReferralLinkHandler = new CreateReferralLinkHandler()
    val candidatesListHandler = new CandidatesListHandler(daoComponent.questionnaireDao, daoComponent.candidateDao)
    val resourceHandler = new ResourceHandler(configComponent.frontendDirectory)
    Future.successful(new HandlerComponent(
      loginHandler,
      authDirective,
      userInfoHandler,
      createReferralLinkHandler,
      resourceHandler,
      questionnaireHandler,
      candidatesListHandler
    ))
  }
}
