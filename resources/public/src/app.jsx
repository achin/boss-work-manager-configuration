/** @jsx React.DOM */

var app = app || {};

(function () {
    'use strict';

    var Choice = React.createClass({
        render: function () {
            var buttonClass, glyphClass, labelClass;

            if (this.props.showAnswer && this.props.type === "real") {
                buttonClass = 'btn btn-default btn-success btn-lg value';
                glyphClass = 'glyphicon glyphicon-ok';
                labelClass = '';
            } else if (this.props.showAnswer && this.props.type === "fake") {
                buttonClass = 'btn btn-default btn-lg value';
                glyphClass = 'glyphicon glyphicon-remove';
                labelClass = 'inactive';
            } else {
                buttonClass = 'btn btn-default btn-lg value';
                glyphClass = 'glyphicon glyphicon-star';
                labelClass = '';
            }

            return (
                <div className='choice'>
                    <h3>
                        <button className={buttonClass}>
                            <span className={glyphClass}></span>
                        </button>
                        <span className={labelClass}>{this.props.value}</span>
                    </h3>
                </div>
            );
        }
    });

    var Score = React.createClass({
        render: function () {
            var correct = this.props.correct;
            var total = this.props.total;
            return (
                <div className='score'>
                    <span className='badge'>{correct} / {total}</span>
                </div>
            );
        }
    });

    var Quiz = React.createClass({
        getInitialState: function () {
            return {choices: [],
                    showAnswer: false,
                    total: 0,
                    correct: 0
            };
        },
        refreshChoices: function () {
            $.get("/quiz", function (data) {
                this.setState({choices: data, showAnswer: false});
            }.bind(this));
        },
        incScore: function(correct) {
            this.setState({total: this.state.total + 1,
                           correct: this.state.correct + (correct ? 1 : 0)}
            );
        },
        componentDidMount: function() {
            this.refreshChoices();
        },
        setShowAnswer: function (bool) {
            this.setState({showAnswer: bool});
        },
        handleFake: function (e) {
            this.handleResponse(e, false);
        },
        handleReal: function (e) {
            this.handleResponse(e, true);
        },
        handleResponse: function (e, correct) {
            e.preventDefault();

            if (this.state.showAnswer) {
                this.refreshChoices();
            } else {
                this.setShowAnswer(true);
                this.incScore(correct);
            }
        },
        whenShowQuestion: function (s) {
            if (!this.state.showAnswer) {
                return s;
            } else {
                return '';
            }
        },
        render: function () {
            var quiz = this;

            return (
                <div>
                    {this.state.choices.map(function (c) {
                        return (
                            <form onSubmit={c.type === 'real' ? quiz.handleReal : quiz.handleFake} key={c.value}>
                                <Choice value={c.value} type={c.type} showAnswer={quiz.state.showAnswer}/>
                            </form>
                        );
                    })}

                    <Score correct={this.state.correct} total={this.state.total} />

                    <button className={'btn btn-primary btn-lg ' + this.whenShowQuestion('hidden')}
                            onClick={this.refreshChoices}>
                        Next
                    </button>
                </div>
            );
        }
    });

    React.renderComponent(<Quiz />, document.getElementById('app'));
})();
