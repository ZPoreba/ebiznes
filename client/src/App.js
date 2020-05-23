import React, { Component } from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import './App.css';
import BaseRouter from './routes';
import Layout from './Common/Layout.js';
import 'antd/dist/antd.css';


class App extends Component {

  // TODO - delete after implementation of authorization
  constructor(props) {
      super(props);

      let user = {
          user_id: 1
      }

      window.localStorage.setItem('user', JSON.stringify(user));
  }

  render() {

    return (
        <div className="App" style={{height: "100%"}}>
          <Router>
            <Layout {...this.props}>
              <BaseRouter />
            </Layout>
          </Router>
        </div>);
  }

}

export default App;
